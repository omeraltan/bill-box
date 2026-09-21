import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "primereact/button";
import { Column } from "primereact/column";
import { DataTable } from "primereact/datatable";
import { Tag } from "primereact/tag";
import { api, toQuery } from "../api/client";
import type { CoverageStatus, PageResponse, WarrantySummary } from "../api/types";
import { PageHeader } from "../components/PageHeader";
import { coverageLabel, dateTr } from "../format";

const filters: { label: string; value?: CoverageStatus }[] = [
  { label: "Tümü" },
  { label: "Geçerli", value: "ACTIVE" },
  { label: "Yaklaşan", value: "EXPIRING" },
  { label: "Biten", value: "EXPIRED" },
];

export function WarrantiesPage() {
  const navigate = useNavigate();
  const [rows, setRows] = useState<WarrantySummary[]>([]);
  const [status, setStatus] = useState<CoverageStatus | undefined>();

  const load = (next?: CoverageStatus) => {
    api<PageResponse<WarrantySummary>>(`/api/warranties${toQuery({ status: next, size: 50 })}`).then((data) => setRows(data.content));
  };

  useEffect(() => {
    load(status);
  }, [status]);

  return (
    <div className="flex flex-column gap-3">
      <PageHeader
        kicker="Ürünler"
        title="Garantiler"
        subtitle="Bitiş tarihi yaklaşan ürünler burada görünür"
        actions={<Button label="Yeni garanti" icon="pi pi-plus" onClick={() => navigate("/garantiler/yeni")} />}
      />
      <div className="panel">
        <div className="vault-filters">
          {filters.map((filter) => (
            <button
              key={filter.label}
              type="button"
              className={`filter-chip ${status === filter.value ? "active" : ""}`}
              onClick={() => setStatus(filter.value)}
            >
              {filter.label}
            </button>
          ))}
        </div>
        <DataTable value={rows} emptyMessage="Garanti belgesi yok." onRowClick={(event) => navigate(`/garantiler/${event.data.id}`)}>
          <Column field="productName" header="Ürün" />
          <Column field="brand" header="Marka" />
          <Column field="merchantName" header="Mağaza" />
          <Column header="Alış" body={(row: WarrantySummary) => dateTr(row.purchasedOn)} />
          <Column header="Bitiş" body={(row: WarrantySummary) => dateTr(row.warrantyEndsOn)} />
          <Column
            header="Kalan"
            body={(row: WarrantySummary) => (
              <div className="remain">
                <span>{row.daysRemaining} gün</span>
                <div className="coverage-track">
                  <div className={`coverage-bar status-${row.status.toLowerCase()}`} style={{ width: `${Math.max(0, Math.min(100, (row.daysRemaining / 365) * 100))}%` }} />
                </div>
              </div>
            )}
          />
          <Column header="Durum" body={(row: WarrantySummary) => <Tag value={coverageLabel[row.status]} severity={row.status === "EXPIRED" ? "danger" : row.status === "EXPIRING" ? "warning" : "success"} />} />
        </DataTable>
      </div>
    </div>
  );
}
