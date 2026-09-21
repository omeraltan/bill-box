import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "primereact/button";
import { Column } from "primereact/column";
import { DataTable } from "primereact/datatable";
import { Tag } from "primereact/tag";
import { api } from "../api/client";
import type { AlertItem } from "../api/types";
import { dateTr, severityLabel } from "../format";

export function AlertsPage() {
  const navigate = useNavigate();
  const [rows, setRows] = useState<AlertItem[]>([]);

  const load = () => api<AlertItem[]>("/api/alerts").then(setRows);

  useEffect(() => {
    load();
  }, []);

  return (
    <div className="flex flex-column gap-3">
      <div className="toolbar">
        <div>
          <h2 className="page-title">Uyarılar</h2>
          <p className="page-subtitle">Vade, gecikme ve yüksek tutar bildirimleri</p>
        </div>
        <Button label="Tümünü okundu işaretle" onClick={async () => { await api("/api/alerts/read-all", { method: "POST" }); load(); }} />
      </div>
      <div className="panel">
        <DataTable value={rows} emptyMessage="Uyarı yok.">
          <Column field="title" header="Başlık" />
          <Column field="message" header="Mesaj" />
          <Column field="severity" header="Önem" body={(row) => <Tag value={severityLabel[row.severity]} severity={row.severity === "CRITICAL" ? "danger" : row.severity === "WARNING" ? "warning" : "info"} />} />
          <Column field="createdAt" header="Tarih" body={(row) => dateTr(row.createdAt)} />
          <Column
            header=""
            body={(row: AlertItem) => (
              <Button
                label={row.readAt ? "Okundu" : "İşaretle"}
                disabled={!!row.readAt}
                onClick={async () => {
                  await api(`/api/alerts/${row.id}/read`, { method: "POST" });
                  if (row.invoiceId) navigate(`/faturalar/${row.invoiceId}`);
                  load();
                }}
              />
            )}
          />
        </DataTable>
      </div>
    </div>
  );
}
