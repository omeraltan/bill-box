import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "primereact/button";
import { Column } from "primereact/column";
import { DataTable } from "primereact/datatable";
import { InputText } from "primereact/inputtext";
import { Tag } from "primereact/tag";
import { api, toQuery } from "../api/client";
import type { PageResponse, VaultItem, VaultKind } from "../api/types";
import { PageHeader } from "../components/PageHeader";
import { coverageLabel, dateTr, kindLabel, money, statusLabel } from "../format";

const filters: { label: string; value: VaultKind | "" }[] = [
  { label: "Tümü", value: "" },
  { label: "Faturalar", value: "BILL" },
  { label: "Fişler", value: "RECEIPT" },
  { label: "Garantiler", value: "WARRANTY" },
];

const routes: Record<VaultKind, string> = {
  BILL: "/faturalar",
  RECEIPT: "/fisler",
  WARRANTY: "/garantiler",
};

function statusText(row: VaultItem) {
  return statusLabel[row.status] ?? coverageLabel[row.status] ?? row.status;
}

export function VaultPage() {
  const navigate = useNavigate();
  const [rows, setRows] = useState<VaultItem[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [kind, setKind] = useState<VaultKind | "">("");
  const [q, setQ] = useState("");

  const load = (nextPage = page, nextKind = kind) => {
    api<PageResponse<VaultItem>>(`/api/vault${toQuery({ kind: nextKind, q, page: nextPage, size: 12 })}`).then((data) => {
      setRows(data.content);
      setTotal(data.totalElements);
    });
  };

  useEffect(() => {
    load(0, kind);
    setPage(0);
  }, [kind]);

  return (
    <div className="flex flex-column gap-3">
      <PageHeader
        kicker="Tek liste"
        title="Tüm belgeler"
        subtitle="Faturalar, fişler ve garanti belgeleri aynı kasada"
        actions={
          <>
            <Button label="Fatura" icon="pi pi-plus" outlined onClick={() => navigate("/faturalar/yeni")} />
            <Button label="Fiş" icon="pi pi-plus" outlined onClick={() => navigate("/fisler/yeni")} />
            <Button label="Garanti" icon="pi pi-plus" onClick={() => navigate("/garantiler/yeni")} />
          </>
        }
      />
      <div className="panel">
        <div className="vault-filters">
          {filters.map((filter) => (
            <button
              key={filter.label}
              type="button"
              className={`filter-chip ${kind === filter.value ? "active" : ""}`}
              onClick={() => setKind(filter.value)}
            >
              {filter.label}
            </button>
          ))}
          <InputText
            value={q}
            placeholder="Ara: mağaza, ürün, fatura no"
            onChange={(event) => setQ(event.target.value)}
            onKeyDown={(event) => event.key === "Enter" && load(0)}
          />
          <Button icon="pi pi-search" rounded text onClick={() => load(0)} />
        </div>
        <DataTable
          value={rows}
          lazy
          paginator
          rows={12}
          totalRecords={total}
          first={page * 12}
          onPage={(event) => {
            const next = event.page ?? 0;
            setPage(next);
            load(next);
          }}
          emptyMessage="Bu kasada henüz belge yok."
          onRowClick={(event) => {
            const row = event.data as VaultItem;
            navigate(`${routes[row.kind]}/${row.id}`);
          }}
          rowClassName={() => "clickable-row"}
        >
          <Column
            header="Tür"
            body={(row: VaultItem) => <span className={`kind-pill kind-${row.kind.toLowerCase()}`}>{kindLabel[row.kind]}</span>}
          />
          <Column field="title" header="Belge" />
          <Column field="subtitle" header="Detay" />
          <Column field="categoryName" header="Kategori" />
          <Column header="Tarih" body={(row: VaultItem) => dateTr(row.occurredOn)} />
          <Column header="Vade / bitiş" body={(row: VaultItem) => dateTr(row.highlightOn)} />
          <Column header="Durum" body={(row: VaultItem) => <Tag value={statusText(row)} />} />
          <Column header="Tutar" body={(row: VaultItem) => (row.kind === "WARRANTY" ? "—" : money(row.amount, row.currency))} />
        </DataTable>
      </div>
    </div>
  );
}
