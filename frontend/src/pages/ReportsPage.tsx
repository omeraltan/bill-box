import { useEffect, useState } from "react";
import { Button } from "primereact/button";
import { Calendar } from "primereact/calendar";
import { Chart } from "primereact/chart";
import { Column } from "primereact/column";
import { DataTable } from "primereact/datatable";
import { Dropdown } from "primereact/dropdown";
import { api, getToken, toQuery } from "../api/client";
import type { InvoiceDirection, ReportSummary } from "../api/types";
import { money } from "../format";

export function ReportsPage() {
  const [from, setFrom] = useState<Date>(new Date(new Date().getFullYear(), 0, 1));
  const [to, setTo] = useState<Date>(new Date());
  const [direction, setDirection] = useState<InvoiceDirection>("EXPENSE");
  const [data, setData] = useState<ReportSummary | null>(null);

  const load = () => {
    api<ReportSummary>(
      `/api/reports/summary${toQuery({
        from: from.toISOString().slice(0, 10),
        to: to.toISOString().slice(0, 10),
        direction,
      })}`
    ).then(setData);
  };

  useEffect(() => {
    load();
  }, []);

  const exportCsv = () => {
    fetch(
      `/api/reports/export${toQuery({
        from: from.toISOString().slice(0, 10),
        to: to.toISOString().slice(0, 10),
        direction,
      })}`,
      { headers: { Authorization: `Bearer ${getToken()}` } }
    )
      .then((response) => response.blob())
      .then((blob) => {
        const url = URL.createObjectURL(blob);
        const link = document.createElement("a");
        link.href = url;
        link.download = "bill-box-rapor.csv";
        link.click();
      });
  };

  return (
    <div className="flex flex-column gap-3">
      <div className="toolbar">
        <div>
          <h2 className="page-title">Raporlar</h2>
          <p className="page-subtitle">Kategori, cari ve dönem kırılımları</p>
        </div>
        <div className="flex gap-2">
          <Calendar value={from} onChange={(e) => setFrom(e.value as Date)} dateFormat="dd.mm.yy" />
          <Calendar value={to} onChange={(e) => setTo(e.value as Date)} dateFormat="dd.mm.yy" />
          <Dropdown value={direction} options={[{ label: "Gider", value: "EXPENSE" }, { label: "Gelir", value: "INCOME" }]} onChange={(e) => setDirection(e.value)} />
          <Button label="Uygula" onClick={load} />
          <Button label="CSV" outlined onClick={exportCsv} />
        </div>
      </div>
      {data && (
        <>
          <div className="kpi-grid">
            <div className="kpi-card">
              <span>Toplam gider</span>
              <strong>{money(data.totalExpense)}</strong>
            </div>
            <div className="kpi-card">
              <span>Toplam gelir</span>
              <strong>{money(data.totalIncome)}</strong>
            </div>
            <div className="kpi-card">
              <span>Net</span>
              <strong>{money(data.totalIncome - data.totalExpense)}</strong>
            </div>
          </div>
          <div className="panel">
            <Chart
              type="bar"
              data={{
                labels: data.byMonth.map((item) => item.name),
                datasets: [{ label: "Tutar", backgroundColor: "#0f766e", data: data.byMonth.map((item) => item.amount) }],
              }}
            />
          </div>
          <div className="grid">
            <div className="col-12 md:col-6">
              <div className="panel">
                <h3>Kategori</h3>
                <DataTable value={data.byCategory}>
                  <Column field="name" header="Ad" />
                  <Column field="amount" header="Tutar" body={(row) => money(row.amount)} />
                </DataTable>
              </div>
            </div>
            <div className="col-12 md:col-6">
              <div className="panel">
                <h3>Cari</h3>
                <DataTable value={data.byVendor}>
                  <Column field="name" header="Ad" />
                  <Column field="amount" header="Tutar" body={(row) => money(row.amount)} />
                </DataTable>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
