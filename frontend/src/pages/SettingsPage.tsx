import { useEffect, useState } from "react";
import { Column } from "primereact/column";
import { DataTable } from "primereact/datatable";
import { api } from "../api/client";
import type { SettingsResponse } from "../api/types";
import { dateTr } from "../format";

export function SettingsPage() {
  const [data, setData] = useState<SettingsResponse | null>(null);

  useEffect(() => {
    api<SettingsResponse>("/api/settings").then(setData);
  }, []);

  if (!data) {
    return <div className="panel">Ayarlar yükleniyor...</div>;
  }

  return (
    <div className="flex flex-column gap-3">
      <div>
        <h2 className="page-title">Ayarlar</h2>
        <p className="page-subtitle">{data.organizationName} · ev ve ekip daveti sonraki adımda açılacak</p>
      </div>
      <div className="panel">
        <h3>Üyeler</h3>
        <DataTable value={data.members}>
          <Column field="fullName" header="Ad" />
          <Column field="email" header="E-posta" />
          <Column field="role" header="Rol" />
        </DataTable>
      </div>
      <div className="panel">
        <h3>Denetim kaydı</h3>
        <DataTable value={data.auditLogs} emptyMessage="Kayıt yok.">
          <Column field="action" header="İşlem" />
          <Column field="entityType" header="Nesne" />
          <Column field="details" header="Detay" />
          <Column field="createdAt" header="Tarih" body={(row) => dateTr(row.createdAt)} />
        </DataTable>
      </div>
    </div>
  );
}
