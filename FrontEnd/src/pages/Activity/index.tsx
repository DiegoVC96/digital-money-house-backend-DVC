import React, { useEffect, useState } from 'react';
import { Button, FormControl, InputLabel, MenuItem, Select } from '@mui/material';
import {
  CardCustom,
  Records,
  RecordVariant,
  IRecord,
  Skeleton,
  SkeletonVariant,
} from '../../components';
import Pagination from '@mui/material/Pagination';
import { usePagination } from '../../hooks/usePagination';
import { ROUTES, UNAUTHORIZED } from '../../constants';
import PaginationItem from '@mui/material/PaginationItem';
import { Link } from 'react-router-dom';
import {
  getUserActivities,
  parseRecordContent,
  pageQuery,
  sortByDate,
} from '../../utils';
import {
  ActivityAmountRange,
  ActivityDirection,
  ActivityFilters,
  Transaction,
} from '../../types';
import { useUserInfo, useLocalStorage, useAuth } from '../../hooks';

const recordsPerPage = 10;
const Activity = () => {
  const [userActivities, setUserActivities] = useState<IRecord[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [filters, setFilters] = useState<ActivityFilters>({});
  const [token] = useLocalStorage('token');

  const { pageNumber, numberOfPages, isRecordsGreeterThanOnePage } =
    usePagination(userActivities as IRecord[], recordsPerPage);
  const { logout } = useAuth();

  const { user } = useUserInfo();

  useEffect(() => {
    if (user && user.id) {
      setIsLoading(true);
      getUserActivities(user.id, token, filters)
        .then((activities) => {
          const orderedActivities = sortByDate(activities);
          const parsedRecords = orderedActivities.map(
            (parsedActivity: Transaction) =>
              parseRecordContent(parsedActivity, RecordVariant.TRANSACTION)
          );
          setUserActivities(parsedRecords);
        })
        .finally(() => setIsLoading(false))
        .catch((error) => {
          if (error.status === UNAUTHORIZED) {
            logout();
          }
        });
    }
  }, [filters, logout, token, user]);

  const updateFilter = <K extends keyof ActivityFilters>(
    key: K,
    value: ActivityFilters[K]
  ) => {
    setFilters((current) => ({ ...current, [key]: value || undefined }));
  };

  const clearFilters = () => setFilters({});

  return (
    <div className="tw-w-full">
      <CardCustom
        className="tw-max-w-5xl"
        content={
          <>
            <div>
              <p className="tw-mb-4 tw-font-bold">Tu actividad</p>
            </div>
            <div className="tw-grid tw-grid-cols-1 md:tw-grid-cols-2 lg:tw-grid-cols-5 tw-gap-4 tw-mb-6">
              <FormControl size="small">
                <InputLabel id="activity-type-label">Tipo</InputLabel>
                <Select
                  labelId="activity-type-label"
                  label="Tipo"
                  value={filters.type || ''}
                  onChange={(event) =>
                    updateFilter(
                      'type',
                      event.target.value as ActivityDirection
                    )
                  }
                >
                  <MenuItem value="">Todos</MenuItem>
                  <MenuItem value="INCOME">Ingresos</MenuItem>
                  <MenuItem value="EXPENSE">Egresos</MenuItem>
                </Select>
              </FormControl>
              <FormControl size="small">
                <InputLabel id="activity-range-label">Importe</InputLabel>
                <Select
                  labelId="activity-range-label"
                  label="Importe"
                  value={filters.range || ''}
                  onChange={(event) =>
                    updateFilter(
                      'range',
                      event.target.value as ActivityAmountRange
                    )
                  }
                >
                  <MenuItem value="">Todos</MenuItem>
                  <MenuItem value="ZERO_TO_1000">Hasta $1.000</MenuItem>
                  <MenuItem value="FROM_1000_TO_5000">$1.000 a $5.000</MenuItem>
                  <MenuItem value="FROM_5000_TO_20000">$5.000 a $20.000</MenuItem>
                  <MenuItem value="FROM_20000_TO_100000">$20.000 a $100.000</MenuItem>
                  <MenuItem value="OVER_100000">Más de $100.000</MenuItem>
                </Select>
              </FormControl>
              <label className="tw-flex tw-flex-col tw-gap-1 tw-text-sm">
                Desde
                <input
                  className="tw-h-10 tw-rounded tw-border tw-border-neutral-blue-100 tw-px-3"
                  type="date"
                  value={filters.from || ''}
                  onChange={(event) => updateFilter('from', event.target.value)}
                />
              </label>
              <label className="tw-flex tw-flex-col tw-gap-1 tw-text-sm">
                Hasta
                <input
                  className="tw-h-10 tw-rounded tw-border tw-border-neutral-blue-100 tw-px-3"
                  type="date"
                  value={filters.to || ''}
                  onChange={(event) => updateFilter('to', event.target.value)}
                />
              </label>
              <Button
                className="tw-self-end"
                variant="outlined"
                onClick={clearFilters}
              >
                Limpiar
              </Button>
            </div>
            {userActivities.length > 0 && !isLoading && (
              <Records
                records={userActivities}
                initialRecord={pageNumber * recordsPerPage - recordsPerPage}
                maxRecords={recordsPerPage * pageNumber}
              />
            )}
            {userActivities.length === 0 && !isLoading && (
              <p>No hay actividad registrada</p>
            )}
            {isLoading && <Skeleton variant={SkeletonVariant.RECORD_LIST} />}
          </>
        }
        actions={
          isRecordsGreeterThanOnePage && (
            <div className="tw-h-12 tw-w-full tw-flex tw-items-center tw-justify-center tw-px-4 tw-mt-4">
              <Pagination
                count={numberOfPages}
                shape="rounded"
                renderItem={(item) => (
                  <PaginationItem
                    component={Link}
                    to={pageQuery(ROUTES.ACTIVITY, item.page as number)}
                    {...item}
                  />
                )}
              />
            </div>
          )
        }
      />
    </div>
  );
};
export default Activity;
