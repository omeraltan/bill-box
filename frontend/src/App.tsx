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
import { SettingsPage } from "./pages/SettingsPage";
import { VendorsPage } from "./pages/VendorsPage";

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
        <Route path="faturalar" element={<InvoicesPage />} />
        <Route path="faturalar/yeni" element={<InvoiceFormPage />} />
        <Route path="faturalar/:id" element={<InvoiceDetailPage />} />
        <Route path="faturalar/:id/duzenle" element={<InvoiceFormPage />} />
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
