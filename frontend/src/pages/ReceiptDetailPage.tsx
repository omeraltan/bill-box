import { useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { Button } from "primereact/button";
import { Column } from "primereact/column";
import { DataTable } from "primereact/datatable";
import { FileUpload } from "primereact/fileupload";
import { Toast } from "primereact/toast";
import { api } from "../api/client";
import type { ReceiptDetail } from "../api/types";
import { PageHeader } from "../components/PageHeader";
import { downloadAuthorized } from "../download";
import { dateTr, money } from "../format";

export function ReceiptDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const toast = useRef<Toast>(null);
  const [receipt, setReceipt] = useState<ReceiptDetail | null>(null);

  const load = () => {
    if (id) api<ReceiptDetail>(`/api/receipts/${id}`).then(setReceipt);
  };

  useEffect(() => {
    load();
  }, [id]);

  if (!receipt || !id) {
    return <div className="panel">Fiş yükleniyor...</div>;
  }

  return (
    <div className="flex flex-column gap-3">
      <Toast ref={toast} />
      <PageHeader
        kicker="Fiş"
        title={receipt.merchantName}
        subtitle={`${dateTr(receipt.purchasedOn)} · ${receipt.categoryName ?? "Kategorisiz"}`}
        actions={
          <>
            <Button label="Düzenle" icon="pi pi-pencil" outlined onClick={() => navigate(`/fisler/${id}/duzenle`)} />
            <Button
              label="Sil"
              icon="pi pi-trash"
              severity="danger"
              outlined
              onClick={async () => {
                await api(`/api/receipts/${id}`, { method: "DELETE" });
                navigate("/fisler");
              }}
            />
          </>
        }
      />
      <div className="kpi-grid">
        <div className="kpi-card">
          <span>Tutar</span>
          <strong>{money(receipt.total, receipt.currency)}</strong>
        </div>
        <div className="kpi-card">
          <span>İade sonu</span>
          <strong>{dateTr(receipt.returnUntil)}</strong>
        </div>
        <div className="kpi-card">
          <span>Ödeme</span>
          <strong>{receipt.paymentMethod || "—"}</strong>
        </div>
        <div className="kpi-card">
          <span>Cari</span>
          <strong>{receipt.vendorName || "—"}</strong>
        </div>
      </div>
      <div className="panel">
        <h3>Kalemler</h3>
        <DataTable value={receipt.items} emptyMessage="Kalem yok. Toplam tutar fişin kendisinde duruyor.">
          <Column field="description" header="Ürün" />
          <Column field="quantity" header="Adet" />
          <Column header="Birim" body={(row) => money(row.unitPrice, receipt.currency)} />
          <Column header="Garanti" body={(row) => (row.warrantyMonths ? `${row.warrantyMonths} ay` : "—")} />
          <Column
            header=""
            body={(row) => (
              <Button
                label="Garanti aç"
                text
                onClick={() => navigate(`/garantiler/yeni?fis=${id}&kalem=${row.id}&ay=${row.warrantyMonths ?? 24}`)}
              />
            )}
          />
        </DataTable>
      </div>
      <div className="panel">
        <div className="toolbar">
          <h3>Fiş görseli</h3>
          <FileUpload
            mode="basic"
            auto
            chooseLabel="Dosya ekle"
            customUpload
            uploadHandler={async (event) => {
              const body = new FormData();
              body.append("file", event.files[0]);
              setReceipt(await api<ReceiptDetail>(`/api/receipts/${id}/files`, { method: "POST", body }));
              toast.current?.show({ severity: "success", summary: "Dosya eklendi" });
            }}
          />
        </div>
        {receipt.files.map((file) => (
          <div key={file.id} className="file-row">
            <span>{file.originalName}</span>
            <Button label="İndir" text icon="pi pi-download" onClick={() => downloadAuthorized(`/api/receipts/${id}/files/${file.id}`, file.originalName)} />
          </div>
        ))}
        {receipt.notes && <p className="note-block">{receipt.notes}</p>}
      </div>
    </div>
  );
}
