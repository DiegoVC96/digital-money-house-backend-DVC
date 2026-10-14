import React, { useCallback, useEffect, useState } from 'react';
import { Button } from '@mui/material';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import {
  CardCustom,
  FormSingle,
  Icon,
  IRecord,
  RecordVariant,
  Records,
  SnackBar,
} from '../../components';
import { currencies, ID, ROUTES, STEP, UNAUTHORIZED } from '../../constants';
import {
  createTransferActivity,
  formatCurrency,
  getRecentRecipients,
  handleChange,
  moneyValidationConfig,
  parseRecordContent,
} from '../../utils';
import { useAuth, useLocalStorage, useUserInfo } from '../../hooks';

const duration = 2500;

const SendMoney = () => {
  const [searchParams] = useSearchParams();
  const { user } = useUserInfo();
  const { logout } = useAuth();
  const [token] = useLocalStorage('token');
  const [recipients, setRecipients] = useState<IRecord[]>([]);

  useEffect(() => {
    if (!user?.id || !token || searchParams.get('step')) return;

    getRecentRecipients(user.id, token)
      .then((items) => setRecipients(items.map((recipient) =>
        parseRecordContent(
          { name: recipient.holderName, origin: recipient.alias },
          RecordVariant.ACCOUNT
        )
      )))
      .catch((error) => {
        if (error.status === UNAUTHORIZED) logout();
      });
  }, [logout, searchParams, token, user]);

  if (searchParams.get('step')) return <SendMoneyForm />;

  return (
    <div className="tw-w-full">
      <CardCustom
        className="tw-max-w-5xl"
        content={<p className="tw-font-bold tw-mb-4">Elegí a qué cuenta transferir</p>}
        actions={
          <Link
            to={`${ROUTES.SEND_MONEY}?${STEP}1`}
            className="tw-w-full tw-flex tw-items-center tw-justify-between tw-p-4 hover:tw-bg-neutral-gray-500 tw-transition"
          >
            <div className="tw-flex tw-items-center tw-gap-x-4"><Icon type="add" /><p>Nueva cuenta</p></div>
            <Icon type="arrow-right" />
          </Link>
        }
      />
      <CardCustom
        className="tw-max-w-5xl"
        content={<>
          <p className="tw-mb-4 tw-font-bold">Últimas cuentas</p>
          {recipients.length ? <Records records={recipients} /> : <p>No hay cuentas registradas</p>}
        </>}
      />
    </div>
  );
};

export default SendMoney;

function SendMoneyForm() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { user } = useUserInfo();
  const { logout } = useAuth();
  const [token] = useLocalStorage('token');
  const step = searchParams.get('step') || '1';
  const [formState, setFormState] = useState({
    destination: searchParams.get('destination') || '',
    amount: '',
  });
  const [error, setError] = useState('');

  const onNavigate = useCallback((nextStep: number) => {
    const query = new URLSearchParams({ step: String(nextStep) });
    if (formState.destination) query.set('destination', formState.destination);
    navigate(`${ROUTES.SEND_MONEY}?${query.toString()}`);
  }, [formState.destination, navigate]);

  const onChange = (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) =>
    handleChange(event, setFormState);

  const submitTransfer = () => {
    const amount = Number(formState.amount);
    if (!user?.id || !token || !formState.destination || !amount) return;

    createTransferActivity(user.id, formState.destination, amount, token)
      .then((transaction) => navigate(`${ROUTES.ACTIVITY_DETAILS}?${ID}${transaction.id}`))
      .catch((requestError) => {
        if (requestError.status === UNAUTHORIZED) logout();
        else if (requestError.status === 410) setError('No contás con saldo suficiente');
        else if (requestError.status === 400) setError('La cuenta de destino no existe o no es válida');
        else setError('No se pudo realizar la transferencia');
      });
  };

  const { locales, currency } = currencies.Argentina;

  if (step === '1') return <>
    <FormSingle
      name="destination"
      title="Agregá una nueva cuenta"
      label="CVU ó Alias"
      type="text"
      actionLabel="Continuar"
      formState={formState}
      handleChange={onChange}
      submit={() => formState.destination && onNavigate(2)}
    />
    {error && <SnackBar duration={duration} message={error} type="error" />}
  </>;

  if (step === '2') return <FormSingle
    name="amount"
    title="¿Cuánto dinero querés transferir?"
    label="Monto"
    type="number"
    actionLabel="Continuar"
    validation={moneyValidationConfig}
    formState={formState}
    handleChange={onChange}
    submit={() => onNavigate(3)}
  />;

  return <>
    <CardCustom
      content={<div className="tw-flex tw-flex-col">
        <p className="tw-font-bold tw-mb-4">Revisá que está todo bien</p>
        <p>Vas a transferir</p>
        <p className="tw-font-bold tw-mb-4">{formatCurrency(locales, currency, Number(formState.amount))}</p>
        <p>Para</p>
        <p className="tw-font-bold tw-mb-4">{formState.destination}</p>
      </div>}
      actions={<div className="tw-flex tw-w-full tw-justify-end tw-mt-6">
        <Button className="tw-h-12 tw-w-64" variant="outlined" onClick={submitTransfer}>Transferir</Button>
      </div>}
    />
    {error && <SnackBar duration={duration} message={error} type="error" />}
  </>;
}
