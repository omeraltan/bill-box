import { useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { Button } from "primereact/button";
import { Column } from "primereact/column";
import { DataTable } from "primereact/datatable";
import { FileUpload } from "primereact/fileupload";
import { Tag } from "primereact/tag";
import { Toast } from "primereact/toast";
import { api, getToken } from "../api/client";
import type { InvoiceDetail, InvoiceStatus } from "../api/types";
import { dateTr, directionLabel, money, statusLabel } from "../format";

export function InvoiceDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const toast = useRef<Toast>(null);
  const [invoice, setInvoice] = useState<InvoiceDetail | null>(null);

  const load = () => {
    if (id) api<InvoiceDetail>(`/api/invoices/${id}`).then(setInvoice);
  };

  useEffect(() => {
    load();
  }, [id]);

  if (!invoice || !id) {
    return <div className="panel">Fatura yükleniyor...</div>;
  }

  const changeStatus = async (status: InvoiceStatus) => {
    const updated = await api<InvoiceDetail>(`/api/invoices/${id}/status`, {
      method: "PATCH",
      body: JSON.stringify({ status }),
    });
    setInvoice(updated);
  };

  const attach = async (file: File) => {
    const body = new FormData();
    body.append("file", file);
    setInvoice(await api<InvoiceDetail>(`/api/invoices/${id}/files`, { method: "POST", body }));
    toast.current?.show({ severity: "success", summary: "Dosya eklendi" });
  };

  const download = (fileId: string) => {
    fetch(`/api/invoices/${id}/files/${fileId}`, {
      headers: { Authorization: `Bearer ${getToken()}` },
    })
      .then((response) => response.blob())
      .then((blob) => {
        const url = URL.createObjectURL(blob);
        window.open(url, "_blank");
      });
  };

  return (
    <div className="flex flex-column gap-3">
      <Toast ref={toast} />
      <div className="toolbar">
        <div>
          <h2 className="page-title">{invoice.invoiceNumber ?? "Fatura"}</h2>
          <p className="page-subtitle">{invoice.vendorName ?? "Cari belirtilmedi"}</p>
        </div>
        <div className="flex gap-2">
          <Button label="Düzenle" icon="pi pi-pencil" onClick={() => navigate(`/faturalar/${id}/duzenle`)} />
          <Button label="Ödendi" icon="pi pi-check" onClick={() => changeStatus("PAID")} />
          <Button
            label="Sil"
            icon="pi pi-trash"
            severity="danger"
            outlined
            onClick={async () => {
              await api(`/api/invoices/${id}`, { method: "DELETE" });
              navigate("/faturalar");
            }}
          />
        </div>
      </div>
      <div className="kpi-grid">
        <div className="kpi-card">
          <span>Yön / durum</span>
          <strong>
            {directionLabel[invoice.direction]} · {statusLabel[invoice.status]}
          </strong>
        </div>
        <div className="kpi-card">
          <span>Tarih / vade</span>
          <strong>
            {dateTr(invoice.issueDate)} / {dateTr(invoice.dueDate)}
          </strong>
        </div>
        <div className="kpi-card">
          <span>Kategori</span>
          <strong>{invoice.categoryName ?? "—"}</strong>
        </div>
        <div className="kpi-card">
          <span>Toplam</span>
          <strong>{money(invoice.total, invoice.currency)}</strong>
        </div>
      </div>
      <div className="panel">
        <h3>Kalemler</h3>
        <DataTable value={invoice.lines} emptyMessage="Kalem yok.">
          <Column field="description" header="Açıklama" />
          <Column field="quantity" header="Miktar" />
          <Column field="unitPrice" header="Birim" body={(row) => money(row.unitPrice, invoice.currency)} />
          <Column field="vatRate" header="KDV %" />
          <Column field="lineTotal" header="Toplam" body={(row) => money(row.lineTotal, invoice.currency)} />
        </DataTable>
      </div>
      <div className="panel">
        <div className="toolbar">
          <h3>Belgeler</h3>
          <FileUpload mode="basic" auto chooseLabel="Dosya ekle" customUpload uploadHandler={(e) => attach(e.files[0])} />
        </div>
        {invoice.files.map((file) => (
          <div key={file.id} className="flex justify-content-between align-items-center mb-2">
            <span>{file.originalName}</span>
            <Button label="İndir" icon="pi pi-download" text onClick={() => download(file.id)} />
          </div>
        ))}
        {invoice.notes && (
          <p>
            <Tag value="Not" /> {invoice.notes}
          </p>
        )}
      </div>
    </div>
  );
}
