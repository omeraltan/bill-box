import { FormEvent, useEffect, useRef, useState } from "react";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import { Button } from "primereact/button";
import { Calendar } from "primereact/calendar";
import { InputText } from "primereact/inputtext";
import { InputTextarea } from "primereact/inputtextarea";
import { Toast } from "primereact/toast";
import { api } from "../api/client";
import type { ReceiptDetail, WarrantyDetail } from "../api/types";
import { PageHeader } from "../components/PageHeader";

function toDate(value?: string | null) {
  return value ? new Date(`${value.slice(0, 10)}T00:00:00`) : null;
}

function toIso(value: Date | null) {
  if (!value) return null;
  const month = String(value.getMonth() + 1).padStart(2, "0");
  const day = String(value.getDate()).padStart(2, "0");
  return `${value.getFullYear()}-${month}-${day}`;
}

function addMonths(date: Date, months: number) {
  const next = new Date(date);
  next.setMonth(next.getMonth() + months);
  return next;
}

export function WarrantyFormPage() {
  const { id } = useParams();
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const toast = useRef<Toast>(null);
  const [receiptId, setReceiptId] = useState<string | null>(params.get("fis"));
  const [productName, setProductName] = useState("");
  const [brand, setBrand] = useState("");
  const [serialNumber, setSerialNumber] = useState("");
  const [merchantName, setMerchantName] = useState("");
  const [purchasedOn, setPurchasedOn] = useState<Date>(new Date());
  const [warrantyEndsOn, setWarrantyEndsOn] = useState<Date>(addMonths(new Date(), 24));
  const [returnUntil, setReturnUntil] = useState<Date | null>(null);
  const [notes, setNotes] = useState("");

  useEffect(() => {
    const receiptParam = params.get("fis");
    const itemParam = params.get("kalem");
    const months = Number(params.get("ay") ?? 24);
    if (receiptParam && itemParam && !id) {
      api<ReceiptDetail>(`/api/receipts/${receiptParam}`).then((receipt) => {
        const item = receipt.items.find((candidate) => candidate.id === itemParam);
        setReceiptId(receipt.id);
        setMerchantName(receipt.merchantName);
        setProductName(item?.description ?? "");
        const purchased = toDate(receipt.purchasedOn) ?? new Date();
        setPurchasedOn(purchased);
        setWarrantyEndsOn(addMonths(purchased, item?.warrantyMonths || months));
        setReturnUntil(toDate(receipt.returnUntil));
      });
    }
    if (id) {
      api<WarrantyDetail>(`/api/warranties/${id}`).then((warranty) => {
        setReceiptId(warranty.receiptId ?? null);
        setProductName(warranty.productName);
        setBrand(warranty.brand ?? "");
        setSerialNumber(warranty.serialNumber ?? "");
        setMerchantName(warranty.merchantName ?? "");
        setPurchasedOn(toDate(warranty.purchasedOn) ?? new Date());
        setWarrantyEndsOn(toDate(warranty.warrantyEndsOn) ?? new Date());
        setReturnUntil(toDate(warranty.returnUntil));
        setNotes(warranty.notes ?? "");
      });
    }
  }, [id, params]);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    try {
      const saved = await api<WarrantyDetail>(id ? `/api/warranties/${id}` : "/api/warranties", {
        method: id ? "PUT" : "POST",
        body: JSON.stringify({
          receiptId,
          productName,
          brand,
          serialNumber,
          merchantName,
          purchasedOn: toIso(purchasedOn),
          warrantyEndsOn: toIso(warrantyEndsOn),
          returnUntil: toIso(returnUntil),
          notes,
        }),
      });
      navigate(`/garantiler/${saved.id}`);
    } catch (error) {
      toast.current?.show({ severity: "error", summary: error instanceof Error ? error.message : "Kayıt başarısız" });
    }
  };

  return (
    <form className="flex flex-column gap-3" onSubmit={submit}>
      <Toast ref={toast} />
      <PageHeader
        kicker="Garanti"
        title={id ? "Belgeyi düzenle" : "Yeni garanti"}
        subtitle="Ürün, seri numarası ve garanti bitişi"
      />
      <div className="panel form-grid">
        <label className="span-2">
          Ürün
          <InputText value={productName} onChange={(event) => setProductName(event.target.value)} required className="w-full" />
        </label>
        <label>
          Marka
          <InputText value={brand} onChange={(event) => setBrand(event.target.value)} className="w-full" />
        </label>
        <label>
          Seri numarası
          <InputText value={serialNumber} onChange={(event) => setSerialNumber(event.target.value)} className="w-full" />
        </label>
        <label>
          Mağaza
          <InputText value={merchantName} onChange={(event) => setMerchantName(event.target.value)} className="w-full" />
        </label>
        <label>
          Alış tarihi
          <Calendar value={purchasedOn} dateFormat="dd.mm.yy" onChange={(event) => setPurchasedOn(event.value as Date)} className="w-full" />
        </label>
        <label>
          Garanti bitişi
          <Calendar value={warrantyEndsOn} dateFormat="dd.mm.yy" onChange={(event) => setWarrantyEndsOn(event.value as Date)} className="w-full" />
        </label>
        <label>
          İade sonu
          <Calendar value={returnUntil} dateFormat="dd.mm.yy" showButtonBar onChange={(event) => setReturnUntil(event.value as Date)} className="w-full" />
        </label>
        <label className="span-2">
          Not
          <InputTextarea value={notes} rows={3} onChange={(event) => setNotes(event.target.value)} className="w-full" />
        </label>
      </div>
      <div className="flex justify-content-end">
        <Button type="submit" label="Kaydet" icon="pi pi-check" />
      </div>
    </form>
  );
}
