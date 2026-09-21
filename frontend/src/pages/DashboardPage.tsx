import { useEffect, useState } from "react";
import { Chart } from "primereact/chart";
import { DataTable } from "primereact/datatable";
import { Column } from "primereact/column";
import { Tag } from "primereact/tag";
import { api } from "../api/client";
import type { Dashboard } from "../api/types";
import { dateTr, directionLabel, money, statusLabel } from "../format";

export function DashboardPage() {
  const [data, setData] = useState<Dashboard | null>(null);

  useEffect(() => {
    api<Dashboard>("/api/dashboard").then(setData);
  }, []);

  if (!data) {
    return <div className="panel">Özet yükleniyor...</div>;
  }

  const categoryChart = {
    labels: data.expenseByCategory.map((item) => item.name),
    datasets: [
      {
        data: data.expenseByCategory.map((item) => item.amount),
        backgroundColor: ["#0f766e", "#c4a35a", "#334155", "#38bdf8", "#f97316", "#a78bfa"],
      },
    ],
  };

  const months = Array.from(new Set([...data.monthlyExpense, ...data.monthlyIncome].map((item) => item.name)));
  const trendChart = {
    labels: months,
    datasets: [
      {
        label: "Gider",
        data: months.map((month) => data.monthlyExpense.find((item) => item.name === month)?.amount ?? 0),
        borderColor: "#0f766e",
        tension: 0.35,
        fill: false,
      },
      {
        label: "Gelir",
        data: months.map((month) => data.monthlyIncome.find((item) => item.name === month)?.amount ?? 0),
        borderColor: "#c4a35a",
        tension: 0.35,
        fill: false,
      },
    ],
  };

  return (
    <div className="flex flex-column gap-3">
      <div>
        <h2 className="page-title">Özet</h2>
        <p className="page-subtitle">Bu ayın nakit akışı, vadeler ve uyarılar</p>
      </div>
      <div className="kpi-grid">
        <div className="kpi-card">
          <span>Bu ay gider</span>
          <strong>{money(data.monthExpense)}</strong>
        </div>
        <div className="kpi-card">
          <span>Bu ay gelir</span>
          <strong>{money(data.monthIncome)}</strong>
        </div>
        <div className="kpi-card">
          <span>Net</span>
          <strong>{money(data.monthNet)}</strong>
        </div>
        <div className="kpi-card">
          <span>Geciken / bekleyen</span>
          <strong>
            {data.overdueCount} / {data.pendingCount}
          </strong>
        </div>
      </div>
      <div className="grid">
        <div className="col-12 lg:col-5">
          <div className="panel">
            <h3>Kategori kırılımı</h3>
            {data.expenseByCategory.length === 0 ? <p>Henüz gider yok.</p> : <Chart type="doughnut" data={categoryChart} />}
          </div>
        </div>
        <div className="col-12 lg:col-7">
          <div className="panel">
            <h3>6 aylık trend</h3>
            <Chart type="line" data={trendChart} />
          </div>
        </div>
      </div>
      <div className="panel">
        <h3>Son faturalar</h3>
        <DataTable value={data.recentInvoices} emptyMessage="Henüz fatura yok.">
          <Column field="invoiceNumber" header="No" />
          <Column field="vendorName" header="Cari" />
          <Column field="issueDate" header="Tarih" body={(row) => dateTr(row.issueDate)} />
          <Column field="direction" header="Yön" body={(row) => directionLabel[row.direction]} />
          <Column field="status" header="Durum" body={(row) => <Tag value={statusLabel[row.status]} />} />
          <Column field="total" header="Tutar" body={(row) => money(row.total, row.currency)} />
        </DataTable>
      </div>
    </div>
  );
}
