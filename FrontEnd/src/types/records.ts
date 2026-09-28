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
