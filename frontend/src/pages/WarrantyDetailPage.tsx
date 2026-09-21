import { useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { Button } from "primereact/button";
import { FileUpload } from "primereact/fileupload";
import { Tag } from "primereact/tag";
import { Toast } from "primereact/toast";
import { api } from "../api/client";
import type { WarrantyDetail } from "../api/types";
import { PageHeader } from "../components/PageHeader";
import { downloadAuthorized } from "../download";
import { coverageLabel, dateTr } from "../format";

export function WarrantyDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const toast = useRef<Toast>(null);
  const [warranty, setWarranty] = useState<WarrantyDetail | null>(null);

  useEffect(() => {
    if (id) api<WarrantyDetail>(`/api/warranties/${id}`).then(setWarranty);
  }, [id]);

  if (!warranty || !id) {
    return <div className="panel">Garanti yükleniyor...</div>;
  }

  const progress = Math.max(0, Math.min(100, (warranty.daysRemaining / 730) * 100));

  return (
    <div className="flex flex-column gap-3">
      <Toast ref={toast} />
      <PageHeader
        kicker="Garanti"
        title={warranty.productName}
        subtitle={[warranty.brand, warranty.merchantName].filter(Boolean).join(" · ") || "Bağlı mağaza yok"}
        actions={
          <>
            <Button label="Düzenle" icon="pi pi-pencil" outlined onClick={() => navigate(`/garantiler/${id}/duzenle`)} />
            <Button
              label="Sil"
              icon="pi pi-trash"
              severity="danger"
              outlined
              onClick={async () => {
                await api(`/api/warranties/${id}`, { method: "DELETE" });
                navigate("/garantiler");
              }}
            />
          </>
        }
      />
      <div className="panel coverage-hero">
        <div>
          <Tag value={coverageLabel[warranty.status]} severity={warranty.status === "EXPIRED" ? "danger" : warranty.status === "EXPIRING" ? "warning" : "success"} />
          <strong>{Math.abs(warranty.daysRemaining)} gün</strong>
          <span>{warranty.daysRemaining >= 0 ? "garanti bitişine kalan" : "garanti biteli"}</span>
        </div>
        <div className="coverage-track wide">
          <div className={`coverage-bar status-${warranty.status.toLowerCase()}`} style={{ width: `${progress}%` }} />
        </div>
      </div>
      <div className="kpi-grid">
        <div className="kpi-card">
          <span>Alış</span>
          <strong>{dateTr(warranty.purchasedOn)}</strong>
        </div>
        <div className="kpi-card">
          <span>Bitiş</span>
          <strong>{dateTr(warranty.warrantyEndsOn)}</strong>
        </div>
        <div className="kpi-card">
          <span>Seri no</span>
          <strong>{warranty.serialNumber || "—"}</strong>
        </div>
        <div className="kpi-card">
          <span>İade sonu</span>
          <strong>{dateTr(warranty.returnUntil)}</strong>
        </div>
      </div>
      <div className="panel">
        <div className="toolbar">
          <h3>Garanti belgesi</h3>
          <FileUpload
            mode="basic"
            auto
            chooseLabel="Belge ekle"
            customUpload
            uploadHandler={async (event) => {
              const body = new FormData();
              body.append("file", event.files[0]);
              setWarranty(await api<WarrantyDetail>(`/api/warranties/${id}/files`, { method: "POST", body }));
              toast.current?.show({ severity: "success", summary: "Belge eklendi" });
            }}
          />
        </div>
        {warranty.files.map((file) => (
          <div key={file.id} className="file-row">
            <span>{file.originalName}</span>
            <Button label="İndir" text icon="pi pi-download" onClick={() => downloadAuthorized(`/api/warranties/${id}/files/${file.id}`, file.originalName)} />
          </div>
        ))}
        {warranty.receiptId && (
          <Button label="Bağlı fişi aç" text icon="pi pi-receipt" onClick={() => navigate(`/fisler/${warranty.receiptId}`)} />
        )}
        {warranty.notes && <p className="note-block">{warranty.notes}</p>}
      </div>
    </div>
  );
}
