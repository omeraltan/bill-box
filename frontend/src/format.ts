export const money = (value?: number | null, currency = "TRY") =>
  new Intl.NumberFormat("tr-TR", { style: "currency", currency }).format(value ?? 0);

export const dateTr = (value?: string | null) => {
  if (!value) return "—";
  return new Intl.DateTimeFormat("tr-TR").format(new Date(value));
};

export const statusLabel: Record<string, string> = {
  DRAFT: "Taslak",
  PENDING: "Bekliyor",
  PAID: "Ödendi",
  OVERDUE: "Gecikti",
  CANCELLED: "İptal",
};

export const directionLabel: Record<string, string> = {
  EXPENSE: "Gider",
  INCOME: "Gelir",
};

export const severityLabel: Record<string, string> = {
  INFO: "Bilgi",
  WARNING: "Uyarı",
  CRITICAL: "Kritik",
};

export const intervalLabel: Record<string, string> = {
  WEEKLY: "Haftalık",
  MONTHLY: "Aylık",
  QUARTERLY: "Üç aylık",
  YEARLY: "Yıllık",
};

export const partyLabel: Record<string, string> = {
  SUPPLIER: "Tedarikçi",
  CUSTOMER: "Müşteri",
  BOTH: "Her ikisi",
};
