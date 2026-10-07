export enum TransactionType {
  Transfer = 'Transfer',
  Deposit = 'Deposit',
}
export interface Transaction {
  amount: number;
  name?: string;
  dated: string;
  id: string;
  type: TransactionType;
  origin?: string;
  destination?: string;
}

export type ActivityDirection = 'INCOME' | 'EXPENSE';

export type ActivityAmountRange =
  | 'ZERO_TO_1000'
  | 'FROM_1000_TO_5000'
  | 'FROM_5000_TO_20000'
  | 'FROM_20000_TO_100000'
  | 'OVER_100000';

export interface ActivityFilters {
  from?: string;
  to?: string;
  type?: ActivityDirection;
  range?: ActivityAmountRange;
}

export interface Card {
  id: string;
  accountId: string;
  lastFour: string;
  brand: string;
  holderName: string;
  expiration: string;
}

export interface Account {
  name: string;
  origin: string;
}

export enum ActivityType {
  TRANSFER_IN = 'transfer-in',
  TRANSFER_OUT = 'transfer-out',
  DEPOSIT = 'deposit',
}
