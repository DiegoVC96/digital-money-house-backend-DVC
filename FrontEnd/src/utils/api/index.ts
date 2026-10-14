import {
  ActivityFilters,
  UserAccount,
  User,
  Transaction,
  TransactionType,
  RecentRecipient,
} from '../../types';

const myInit = (method = 'GET', token?: string) => {
  return {
    method,
    headers: {
      'Content-Type': 'application/json',
      Authorization: token ? `Bearer ${token}` : '',
    },
    mode: 'cors' as RequestMode,
    cache: 'default' as RequestCache,
  };
};

const myRequest = (endpoint: string, method: string, token?: string) =>
  new Request(endpoint, myInit(method, token));

const baseUrl = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api';

const rejectPromise = async <T = never>(response?: Response): Promise<T> => {
  let status = response?.status || '00';
  let statusText = response?.statusText || 'Ocurrió un error';

  try {
    const apiError = await response?.clone().json();
    statusText = apiError.message || statusText;
  } catch {
    // La respuesta puede no tener cuerpo JSON; se conserva el estado HTTP.
  }

  return Promise.reject({ status, statusText, err: true });
};

export const login = (email: string, password: string) => {
  return fetch(myRequest(`${baseUrl}/auth/login`, 'POST'), {
    body: JSON.stringify({ email, password }),
  })
    .then((response) => {
      if (response.ok) {
        return response.json();
      }
      return rejectPromise(response);
    })
    .catch((err) => {
      console.log(err);
      return Promise.reject(err);
    });
};

export const logout = (token: string) => {
  return fetch(myRequest(`${baseUrl}/auth/logout`, 'POST', token))
    .then((response) => {
      if (response.ok) {
        return;
      }

      return rejectPromise(response);
    })
    .catch((err) => {
      console.log(err);
      return rejectPromise(err);
    });
};

export const createAnUser = (user: User) => {
  return fetch(myRequest(`${baseUrl}/auth/register`, 'POST'), {
    body: JSON.stringify(user),
  })
    .then((response) =>
      response.ok ? response.json() : rejectPromise(response)
    )
    .catch((err) => {
      console.log(err);
      return rejectPromise(err);
    });
};

export const getUser = (
  id: string,
  token: string
): Promise<User> => {
  return fetch(myRequest(`${baseUrl}/users/${id}`, 'GET', token))
    .then((response) =>
      response.ok ? response.json() : rejectPromise(response)
    )
    .catch((err) => {
      console.log(err);
      return rejectPromise(err);
    });
};

export const updateUser = (
  id: string,
  data: any,
  token: string
): Promise<Response> => {
  return fetch(myRequest(`${baseUrl}/users/${id}`, 'PATCH', token), {
    body: JSON.stringify(data),
  })
    .then((response) =>
      response.ok ? response.json() : rejectPromise(response)
    )
    .catch((err) => {
      console.log(err);
      return rejectPromise(err);
    });
};


export const getAccount = (
  id: string,
  token: string
): Promise<UserAccount> => {
  return fetch(myRequest(`${baseUrl}/accounts/me/${id}`, 'GET', token))
    .then((response) => {
      if (!response.ok) {
        return rejectPromise(response);
      }

      return response.json().then((account) => ({
        ...account,
        name: account.holderName,
      }));
    })
    .catch((err) => {
      console.log(err);
      return rejectPromise(err);
    });
};

export const getRecentTransactions = (
  userId: string,
  token: string,
  limit = 5
): Promise<Transaction[]> => {
  return getAccount(userId, token)
    .then((account) =>
      fetch(
        myRequest(
          `${baseUrl}/accounts/${account.id}/transactions?limit=${limit}`,
          'GET',
          token
        )
      )
    )
    .then((response) =>
      response.ok ? response.json() : rejectPromise(response)
    )
    .then((transactions) => transactions.map(toTransaction))
    .catch((err) => {
      console.log(err);
      return rejectPromise(err);
    });
};

export const getAccounts = (): Promise<UserAccount[]> => {
  return fetch(myRequest(`${baseUrl}/accounts`, 'GET'))
    .then((response) =>
      response.ok ? response.json() : rejectPromise(response)
    )
    .catch((err) => {
      console.log(err);
      return rejectPromise(err);
    });
};

export const updateAccount = (
  userId: string,
  data: { alias: string },
  token: string
): Promise<UserAccount> => {
  return getAccount(userId, token)
    .then((account) =>
      fetch(
        myRequest(`${baseUrl}/accounts/${account.id}`, 'PATCH', token),
        {
          body: JSON.stringify(data),
        }
      )
    )
    .then((response) => {
      if (!response.ok) {
        return rejectPromise(response);
      }

      return response.json().then((account) => ({
        ...account,
        name: account.holderName,
      }));
    })
    .catch((err) => {
      console.log(err);
      return rejectPromise(err);
    });
};

export const getUserActivities = (
  userId: string,
  token: string,
  filters: ActivityFilters = {}
): Promise<Transaction[]> => {
  const query = new URLSearchParams();

  if (filters.from) {
    query.set('from', `${filters.from}T00:00:00Z`);
  }

  if (filters.to) {
    query.set('to', `${filters.to}T23:59:59.999Z`);
  }

  if (filters.type) {
    query.set('type', filters.type);
  }

  if (filters.range) {
    query.set('range', filters.range);
  }

  const queryString = query.toString();
  const suffix = queryString ? `?${queryString}` : '';

  return getAccount(userId, token)
    .then((account) =>
      fetch(
        myRequest(
          `${baseUrl}/accounts/${account.id}/activity${suffix}`,
          'GET',
          token
        )
      )
    )
    .then((response) => {
      if (response.ok) {
        return response.json().then((transactions) => transactions.map(toTransaction));
      }
      return rejectPromise(response);
    })
    .catch((err) => {
      console.log(err);
      return rejectPromise(err);
    });
};

export const getUserActivity = (
  userId: string,
  activityId: string,
  token: string
): Promise<Transaction> => {
  return getAccount(userId, token)
    .then((account) =>
      fetch(
        myRequest(
          `${baseUrl}/accounts/${account.id}/activity/${activityId}`,
          'GET',
          token
        )
      )
    )
    .then((response) => {
      if (response.ok) {
        return response.json().then(toTransaction);
      }
      return rejectPromise(response);
    })
    .catch((err) => {
      console.log(err);
      return Promise.reject(err);
    });
};

export const getUserCards = (userId: string, token: string) => {
  return getAccount(userId, token)
    .then((account) =>
      fetch(
        myRequest(`${baseUrl}/accounts/${account.id}/cards`, 'GET', token)
      )
    )
    .then((response) =>
      response.ok ? response.json() : rejectPromise(response)
    )
    .catch((err) => {
      console.log(err);
      return rejectPromise(err);
    });
};

export const getUserCard = (
  userId: string,
  cardId: string,
  token: string
) => {
  return getAccount(userId, token)
    .then((account) =>
      fetch(
        myRequest(
          `${baseUrl}/accounts/${account.id}/cards/${cardId}`,
          'GET',
          token
        )
      )
    )
    .then((response) =>
      response.ok ? response.json() : rejectPromise(response)
    )
    .catch((err) => {
      console.log(err);
      return rejectPromise(err);
    });
};

export const deleteUserCard = (
  userId: string,
  cardId: string,
  token: string
) => {
  return getAccount(userId, token)
    .then((account) =>
      fetch(
        myRequest(
          `${baseUrl}/accounts/${account.id}/cards/${cardId}`,
          'DELETE',
          token
        )
      )
    )
    .then((response) => {
      if (!response.ok) {
        return rejectPromise(response);
      }
    })
    .catch((err) => {
      console.log(err);
      return rejectPromise(err);
    });
};

export const createUserCard = (
  userId: string,
  card: {
    number: string;
    name: string;
    expiration: string;
    cvc: string;
  },
  token: string
) => {
  return getAccount(userId, token)
    .then((account) =>
      fetch(
        myRequest(`${baseUrl}/accounts/${account.id}/cards`, 'POST', token),
        {
          body: JSON.stringify(card),
        }
      )
    )
    .then((response) =>
      response.ok ? response.json() : rejectPromise(response)
    )
    .catch((err) => {
      console.log(err);
      return Promise.reject(err);
    });
};

export const createDepositActivity = (
  userId: string,
  cardId: string,
  amount: number,
  token: string
) : Promise<Transaction> => {
  return getAccount(userId, token)
    .then((account) =>
      fetch(
        myRequest(
          `${baseUrl}/accounts/${account.id}/transferences`,
          'POST',
          token
        ),
        {
          body: JSON.stringify({ cardId, amount }),
        }
      )
    )
    .then((response) =>
      response.ok ? response.json() : rejectPromise(response)
    )
    .then(toTransaction)
    .catch((err) => {
      console.log(err);
      return Promise.reject(err);
    });
};

const toTransaction = (transaction: any): Transaction => ({
  id: transaction.id,
  amount:
    transaction.type === 'TRANSFER_OUT'
      ? -transaction.amount
      : transaction.amount,
  name: transaction.description,
  dated: transaction.createdAt,
  type:
    transaction.type === 'DEPOSIT'
      ? TransactionType.Deposit
      : TransactionType.Transfer,
});

export const createTransferActivity = (
  userId: string,
  destination: string,
  amount: number,
  token: string
): Promise<Transaction> => {
  return getAccount(userId, token)
    .then((account) => fetch(
      myRequest(`${baseUrl}/accounts/${account.id}/transferences`, 'POST', token),
      { body: JSON.stringify({ destination, amount }) }
    ))
    .then((response) =>
      response.ok ? response.json() : rejectPromise(response)
    )
    .then(toTransaction)
    .catch((err) => {
      console.log(err);
      return Promise.reject(err);
    });
};

export const getRecentRecipients = (
  userId: string,
  token: string
): Promise<RecentRecipient[]> => {
  return getAccount(userId, token)
    .then((account) => fetch(
      myRequest(`${baseUrl}/accounts/${account.id}/transferences`, 'GET', token)
    ))
    .then((response) =>
      response.ok ? response.json() : rejectPromise(response)
    )
    .catch((err) => {
      console.log(err);
      return Promise.reject(err);
    });
};
