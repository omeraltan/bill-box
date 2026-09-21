import { FormEvent, useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { Button } from "primereact/button";
import { Calendar } from "primereact/calendar";
import { Dropdown } from "primereact/dropdown";
import { InputNumber } from "primereact/inputnumber";
import { InputText } from "primereact/inputtext";
import { InputTextarea } from "primereact/inputtextarea";
import { Toast } from "primereact/toast";
import { api } from "../api/client";
import type { Category, ReceiptDetail, ReceiptItem, Vendor } from "../api/types";
import { PageHeader } from "../components/PageHeader";

const emptyItem = (): ReceiptItem => ({ description: "", quantity: 1, unitPrice: 0, warrantyMonths: null });

function toDate(value?: string) {
  return value ? new Date(`${value}T00:00:00`) : null;
}

function toIso(value: Date | null) {
  if (!value) return null;
  const month = String(value.getMonth() + 1).padStart(2, "0");
  const day = String(value.getDate()).padStart(2, "0");
  return `${value.getFullYear()}-${month}-${day}`;
}

export function ReceiptFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const toast = useRef<Toast>(null);
  const [vendors, setVendors] = useState<Vendor[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [merchantName, setMerchantName] = useState("");
  const [vendorId, setVendorId] = useState<string | null>(null);
  const [categoryId, setCategoryId] = useState<string | null>(null);
  const [purchasedOn, setPurchasedOn] = useState<Date>(new Date());
  const [returnUntil, setReturnUntil] = useState<Date | null>(null);
  const [paymentMethod, setPaymentMethod] = useState("");
  const [total, setTotal] = useState(0);
  const [notes, setNotes] = useState("");
  const [items, setItems] = useState<ReceiptItem[]>([emptyItem()]);

  useEffect(() => {
    api<Vendor[]>("/api/vendors/all").then(setVendors);
    api<Category[]>("/api/categories").then(setCategories);
    if (id) {
      api<ReceiptDetail>(`/api/receipts/${id}`).then((receipt) => {
        setMerchantName(receipt.merchantName);
        setVendorId(receipt.vendorId ?? null);
        setCategoryId(receipt.categoryId ?? null);
        setPurchasedOn(toDate(receipt.purchasedOn) ?? new Date());
        setReturnUntil(toDate(receipt.returnUntil));
        setPaymentMethod(receipt.paymentMethod ?? "");
        setTotal(receipt.total);
        setNotes(receipt.notes ?? "");
        setItems(receipt.items.length ? receipt.items : [emptyItem()]);
      });
    }
  }, [id]);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    try {
      const saved = await api<ReceiptDetail>(id ? `/api/receipts/${id}` : "/api/receipts", {
        method: id ? "PUT" : "POST",
        body: JSON.stringify({
          merchantName,
          vendorId,
          categoryId,
          purchasedOn: toIso(purchasedOn),
          returnUntil: toIso(returnUntil),
          paymentMethod,
          total,
          currency: "TRY",
          notes,
          items,
        }),
      });
      navigate(`/fisler/${saved.id}`);
    } catch (error) {
      toast.current?.show({ severity: "error", summary: error instanceof Error ? error.message : "Kayıt başarısız" });
    }
  };

  return (
    <form className="flex flex-column gap-3" onSubmit={submit}>
      <Toast ref={toast} />
      <PageHeader kicker="Fiş" title={id ? "Fişi düzenle" : "Yeni fiş"} subtitle="Mağaza, kalemler ve varsa iade süresi" />
      <div className="panel form-grid">
        <label>
          Mağaza
          <InputText value={merchantName} onChange={(event) => setMerchantName(event.target.value)} required className="w-full" />
        </label>
        <label>
          Cari
          <Dropdown value={vendorId} options={vendors} optionLabel="name" optionValue="id" showClear placeholder="İsteğe bağlı" onChange={(event) => setVendorId(event.value)} className="w-full" />
        </label>
        <label>
          Kategori
          <Dropdown value={categoryId} options={categories.filter((item) => item.kind !== "INCOME")} optionLabel="name" optionValue="id" showClear onChange={(event) => setCategoryId(event.value)} className="w-full" />
        </label>
        <label>
          Alış tarihi
          <Calendar value={purchasedOn} dateFormat="dd.mm.yy" onChange={(event) => setPurchasedOn(event.value as Date)} className="w-full" />
        </label>
        <label>
          İade son tarihi
          <Calendar value={returnUntil} dateFormat="dd.mm.yy" showButtonBar onChange={(event) => setReturnUntil(event.value as Date)} className="w-full" />
        </label>
        <label>
          Ödeme
          <InputText value={paymentMethod} onChange={(event) => setPaymentMethod(event.target.value)} className="w-full" placeholder="Kart, nakit" />
        </label>
        <label className="span-2">
          Toplam
          <InputNumber value={total} mode="currency" currency="TRY" locale="tr-TR" onValueChange={(event) => setTotal(event.value ?? 0)} className="w-full" />
          <small>Kalem girersen toplam kalemlerden hesaplanır.</small>
        </label>
        <label className="span-2">
          Not
          <InputTextarea value={notes} rows={3} onChange={(event) => setNotes(event.target.value)} className="w-full" />
        </label>
      </div>
      <div className="panel">
        <div className="toolbar">
          <h3>Kalemler</h3>
          <Button type="button" label="Kalem ekle" text icon="pi pi-plus" onClick={() => setItems((current) => [...current, emptyItem()])} />
        </div>
        {items.map((item, index) => (
          <div className="item-row" key={index}>
            <InputText
              value={item.description}
              placeholder="Ürün"
              onChange={(event) => {
                const next = [...items];
                next[index] = { ...item, description: event.target.value };
                setItems(next);
              }}
            />
            <InputNumber
              value={item.quantity}
              placeholder="Adet"
              onValueChange={(event) => {
                const next = [...items];
                next[index] = { ...item, quantity: event.value ?? 1 };
                setItems(next);
              }}
            />
            <InputNumber
              value={item.unitPrice}
              mode="currency"
              currency="TRY"
              locale="tr-TR"
              onValueChange={(event) => {
                const next = [...items];
                next[index] = { ...item, unitPrice: event.value ?? 0 };
                setItems(next);
              }}
            />
            <InputNumber
              value={item.warrantyMonths}
              suffix=" ay"
              placeholder="Garanti"
              onValueChange={(event) => {
                const next = [...items];
                next[index] = { ...item, warrantyMonths: event.value };
                setItems(next);
              }}
            />
            <Button type="button" icon="pi pi-trash" text severity="danger" onClick={() => setItems(items.filter((_, itemIndex) => itemIndex !== index))} />
          </div>
        ))}
      </div>
      <div className="flex justify-content-end">
        <Button type="submit" label="Kaydet" icon="pi pi-check" />
      </div>
    </form>
  );
}
