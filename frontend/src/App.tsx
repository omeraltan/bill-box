import type { ReactNode } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { useAuth } from "./auth/AuthContext";
import { AppLayout } from "./layout/AppLayout";
import { AlertsPage } from "./pages/AlertsPage";
import { BudgetsPage } from "./pages/BudgetsPage";
import { CategoriesPage } from "./pages/CategoriesPage";
import { DashboardPage } from "./pages/DashboardPage";
import { InvoiceDetailPage } from "./pages/InvoiceDetailPage";
import { InvoiceFormPage } from "./pages/InvoiceFormPage";
import { InvoicesPage } from "./pages/InvoicesPage";
import { LoginPage } from "./pages/LoginPage";
import { RecurringPage } from "./pages/RecurringPage";
import { RegisterPage } from "./pages/RegisterPage";
import { ReportsPage } from "./pages/ReportsPage";
import { ReceiptDetailPage } from "./pages/ReceiptDetailPage";
import { ReceiptFormPage } from "./pages/ReceiptFormPage";
import { ReceiptsPage } from "./pages/ReceiptsPage";
import { SettingsPage } from "./pages/SettingsPage";
import { VaultPage } from "./pages/VaultPage";
import { VendorsPage } from "./pages/VendorsPage";
import { WarrantiesPage } from "./pages/WarrantiesPage";
import { WarrantyDetailPage } from "./pages/WarrantyDetailPage";
import { WarrantyFormPage } from "./pages/WarrantyFormPage";

function Protected({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  if (!user) {
    return <Navigate to="/giris" replace />;
  }
  return children;
}

export default function App() {
  return (
    <Routes>
      <Route path="/giris" element={<LoginPage />} />
      <Route path="/kayit" element={<RegisterPage />} />
      <Route
        path="/"
        element={
          <Protected>
            <AppLayout />
          </Protected>
        }
      >
        <Route index element={<DashboardPage />} />
        <Route path="kasa" element={<VaultPage />} />
        <Route path="faturalar" element={<InvoicesPage />} />
        <Route path="faturalar/yeni" element={<InvoiceFormPage />} />
        <Route path="faturalar/:id" element={<InvoiceDetailPage />} />
        <Route path="faturalar/:id/duzenle" element={<InvoiceFormPage />} />
        <Route path="fisler" element={<ReceiptsPage />} />
        <Route path="fisler/yeni" element={<ReceiptFormPage />} />
        <Route path="fisler/:id" element={<ReceiptDetailPage />} />
        <Route path="fisler/:id/duzenle" element={<ReceiptFormPage />} />
        <Route path="garantiler" element={<WarrantiesPage />} />
        <Route path="garantiler/yeni" element={<WarrantyFormPage />} />
        <Route path="garantiler/:id" element={<WarrantyDetailPage />} />
        <Route path="garantiler/:id/duzenle" element={<WarrantyFormPage />} />
        <Route path="cariler" element={<VendorsPage />} />
        <Route path="kategoriler" element={<CategoriesPage />} />
        <Route path="tekrarlayan" element={<RecurringPage />} />
        <Route path="butceler" element={<BudgetsPage />} />
        <Route path="raporlar" element={<ReportsPage />} />
        <Route path="uyarilar" element={<AlertsPage />} />
        <Route path="ayarlar" element={<SettingsPage />} />
      </Route>
    </Routes>
  );
}
