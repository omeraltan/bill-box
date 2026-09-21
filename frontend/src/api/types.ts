export type InvoiceDirection = "EXPENSE" | "INCOME";
export type InvoiceStatus = "DRAFT" | "PENDING" | "PAID" | "OVERDUE" | "CANCELLED";
export type PartyType = "SUPPLIER" | "CUSTOMER" | "BOTH";
export type CategoryKind = "EXPENSE" | "INCOME" | "BOTH";
export type RecurringInterval = "WEEKLY" | "MONTHLY" | "QUARTERLY" | "YEARLY";
export type AlertType = "DUE_SOON" | "OVERDUE" | "HIGH_AMOUNT" | "BUDGET_EXCEEDED" | "PRICE_INCREASE";
export type AlertSeverity = "INFO" | "WARNING" | "CRITICAL";

export interface AuthResponse {
  token: string;
  userId: string;
  organizationId: string;
  fullName: string;
  email: string;
  organizationName: string;
  role: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface Vendor {
  id: string;
  name: string;
  taxNumber?: string;
  email?: string;
  phone?: string;
  iban?: string;
  address?: string;
  partyType: PartyType;
  notes?: string;
}

export interface Category {
  id: string;
  name: string;
  kind: CategoryKind;
  color: string;
  icon?: string;
}

export interface InvoiceLine {
  id?: string;
  description: string;
  quantity: number;
  unitPrice: number;
  vatRate: number;
  lineTotal?: number;
  sortOrder?: number;
}

export interface InvoiceFile {
  id: string;
  originalName: string;
  contentType: string;
  sizeBytes: number;
  createdAt: string;
}

export interface InvoiceSummary {
  id: string;
  direction: InvoiceDirection;
  status: InvoiceStatus;
  invoiceNumber?: string;
  issueDate: string;
  dueDate?: string;
  paidDate?: string;
  currency: string;
  total: number;
  vendorName?: string;
  categoryName?: string;
  categoryColor?: string;
}

export interface InvoiceDetail extends InvoiceSummary {
  subtotal: number;
  vatAmount: number;
  total: number;
  paymentMethod?: string;
  notes?: string;
  vendorId?: string;
  categoryId?: string;
  lines: InvoiceLine[];
  files: InvoiceFile[];
}

export interface NamedAmount {
  name: string;
  amount: number;
}

export interface AlertItem {
  id: string;
  type: AlertType;
  severity: AlertSeverity;
  title: string;
  message: string;
  invoiceId?: string;
  createdAt: string;
  readAt?: string;
}

export interface Dashboard {
  monthExpense: number;
  monthIncome: number;
  monthNet: number;
  overdueCount: number;
  pendingCount: number;
  unreadAlerts: number;
  expenseByCategory: NamedAmount[];
  monthlyExpense: NamedAmount[];
  monthlyIncome: NamedAmount[];
  recentInvoices: InvoiceSummary[];
  latestAlerts: AlertItem[];
}

export interface RecurringRule {
  id: string;
  vendorId?: string;
  vendorName?: string;
  categoryId?: string;
  categoryName?: string;
  direction: InvoiceDirection;
  title: string;
  amount: number;
  currency: string;
  interval: RecurringInterval;
  nextDueDate: string;
  dayOfMonth?: number;
  active: boolean;
  notes?: string;
}

export interface Budget {
  id: string;
  categoryId?: string;
  categoryName: string;
  year: number;
  month?: number;
  amount: number;
  currency: string;
}

export interface ReportSummary {
  totalExpense: number;
  totalIncome: number;
  byCategory: NamedAmount[];
  byVendor: NamedAmount[];
  byMonth: NamedAmount[];
}

export interface OcrResult {
  vendorName?: string;
  taxNumber?: string;
  invoiceNumber?: string;
  issueDate?: string;
  dueDate?: string;
  iban?: string;
  currency?: string;
  subtotal?: number;
  vatAmount?: number;
  total?: number;
  rawText?: string;
  warnings: string[];
}

export interface SettingsResponse {
  organizationName: string;
  members: { userId: string; fullName: string; email: string; role: string }[];
  auditLogs: { id: string; action: string; entityType: string; details?: string; createdAt: string }[];
}
