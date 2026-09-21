import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "primereact/button";
import { Column } from "primereact/column";
import { DataTable } from "primereact/datatable";
import { InputText } from "primereact/inputtext";
import { api, toQuery } from "../api/client";
import type { PageResponse, ReceiptSummary } from "../api/types";
import { PageHeader } from "../components/PageHeader";
import { dateTr, money } from "../format";

export function ReceiptsPage() {
  const navigate = useNavigate();
  const [rows, setRows] = useState<ReceiptSummary[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [q, setQ] = useState("");

  const load = (nextPage = page) => {
    api<PageResponse<ReceiptSummary>>(`/api/receipts${toQuery({ q, page: nextPage, size: 12 })}`).then((data) => {
      setRows(data.content);
      setTotal(data.totalElements);
    });
  };

  useEffect(() => {
    load(0);
  }, []);

  return (
    <div className="flex flex-column gap-3">
      <PageHeader
        kicker="Alışveriş"
        title="Fişler"
        subtitle="Market, elektronik ve diğer ürün fişleri"
        actions={<Button label="Yeni fiş" icon="pi pi-plus" onClick={() => navigate("/fisler/yeni")} />}
      />
      <div className="panel">
        <div className="vault-filters">
          <InputText value={q} placeholder="Mağaza ara" onChange={(event) => setQ(event.target.value)} onKeyDown={(event) => event.key === "Enter" && load(0)} />
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
          emptyMessage="Henüz fiş yok."
          onRowClick={(event) => navigate(`/fisler/${event.data.id}`)}
        >
          <Column field="merchantName" header="Mağaza" />
          <Column field="categoryName" header="Kategori" />
          <Column header="Tarih" body={(row: ReceiptSummary) => dateTr(row.purchasedOn)} />
          <Column header="İade sonu" body={(row: ReceiptSummary) => dateTr(row.returnUntil)} />
          <Column field="itemCount" header="Kalem" />
          <Column header="Tutar" body={(row: ReceiptSummary) => money(row.total, row.currency)} />
        </DataTable>
      </div>
    </div>
  );
}
