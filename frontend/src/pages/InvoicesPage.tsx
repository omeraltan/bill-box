import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "primereact/button";
import { Column } from "primereact/column";
import { DataTable } from "primereact/datatable";
import { Dropdown } from "primereact/dropdown";
import { InputText } from "primereact/inputtext";
import { Tag } from "primereact/tag";
import { Toast } from "primereact/toast";
import { FileUpload } from "primereact/fileupload";
import { api, toQuery } from "../api/client";
import type { InvoiceDirection, InvoiceStatus, InvoiceSummary, PageResponse } from "../api/types";
import { dateTr, directionLabel, money, statusLabel } from "../format";

export function InvoicesPage() {
  const navigate = useNavigate();
  const toast = useRef<Toast>(null);
  const [rows, setRows] = useState<InvoiceSummary[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [q, setQ] = useState("");
  const [direction, setDirection] = useState<InvoiceDirection | null>(null);
  const [status, setStatus] = useState<InvoiceStatus | null>(null);

  const load = () => {
    api<PageResponse<InvoiceSummary>>(
      `/api/invoices${toQuery({ q, direction, status, page, size: 15 })}`
    ).then((data) => {
      setRows(data.content);
      setTotal(data.totalElements);
    });
  };

  useEffect(() => {
    load();
  }, [page, direction, status]);

  const importUbl = async (file: File, invoiceDirection: InvoiceDirection) => {
    const body = new FormData();
    body.append("file", file);
    body.append("direction", invoiceDirection);
    try {
      const created = await api<{ id: string }>(`/api/ubl/import`, { method: "POST", body });
      toast.current?.show({ severity: "success", summary: "UBL içe aktarıldı" });
      navigate(`/faturalar/${created.id}`);
    } catch (err) {
      toast.current?.show({
        severity: "error",
        summary: err instanceof Error ? err.message : "İçe aktarma başarısız",
      });
    }
  };

  return (
    <div className="flex flex-column gap-3">
      <Toast ref={toast} />
      <div className="toolbar">
        <div>
          <h2 className="page-title">Faturalar</h2>
          <p className="page-subtitle">Gider ve gelir faturalarınızı arayın, içe aktarın, yönetin</p>
        </div>
        <div className="flex gap-2">
          <FileUpload
            mode="basic"
            auto
            chooseLabel="UBL gider"
            customUpload
            uploadHandler={(e) => importUbl(e.files[0], "EXPENSE")}
          />
          <FileUpload
            mode="basic"
            auto
            chooseLabel="UBL gelir"
            customUpload
            uploadHandler={(e) => importUbl(e.files[0], "INCOME")}
          />
          <Button label="Yeni fatura" icon="pi pi-plus" onClick={() => navigate("/faturalar/yeni")} />
        </div>
      </div>
      <div className="panel">
        <div className="flex flex-wrap gap-2 mb-3">
          <InputText value={q} onChange={(e) => setQ(e.target.value)} placeholder="No, cari, not" onKeyDown={(e) => e.key === "Enter" && load()} />
          <Dropdown
            value={direction}
            onChange={(e) => setDirection(e.value)}
            options={[
              { label: "Tümü", value: null },
              { label: "Gider", value: "EXPENSE" },
              { label: "Gelir", value: "INCOME" },
            ]}
            placeholder="Yön"
          />
          <Dropdown
            value={status}
            onChange={(e) => setStatus(e.value)}
            options={[
              { label: "Tümü", value: null },
              ...Object.entries(statusLabel).map(([value, label]) => ({ label, value })),
            ]}
            placeholder="Durum"
          />
          <Button label="Ara" icon="pi pi-search" onClick={load} />
        </div>
        <DataTable
          value={rows}
          lazy
          paginator
          rows={15}
          totalRecords={total}
          first={page * 15}
          onPage={(e) => setPage((e.page ?? 0))}
          onRowClick={(e) => navigate(`/faturalar/${e.data.id}`)}
          emptyMessage="Kayıt bulunamadı."
        >
          <Column field="invoiceNumber" header="No" />
          <Column field="vendorName" header="Cari" />
          <Column field="categoryName" header="Kategori" />
          <Column field="issueDate" header="Tarih" body={(row) => dateTr(row.issueDate)} />
          <Column field="dueDate" header="Vade" body={(row) => dateTr(row.dueDate)} />
          <Column field="direction" header="Yön" body={(row) => directionLabel[row.direction]} />
          <Column field="status" header="Durum" body={(row) => <Tag value={statusLabel[row.status]} />} />
          <Column field="total" header="Tutar" body={(row) => money(row.total, row.currency)} />
        </DataTable>
      </div>
    </div>
  );
}
