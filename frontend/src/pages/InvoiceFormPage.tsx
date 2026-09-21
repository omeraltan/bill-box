import { FormEvent, useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { Button } from "primereact/button";
import { Calendar } from "primereact/calendar";
import { Dropdown } from "primereact/dropdown";
import { FileUpload } from "primereact/fileupload";
import { InputNumber } from "primereact/inputnumber";
import { InputText } from "primereact/inputtext";
import { InputTextarea } from "primereact/inputtextarea";
import { Toast } from "primereact/toast";
import { api } from "../api/client";
import type { Category, InvoiceDetail, InvoiceDirection, InvoiceLine, InvoiceStatus, OcrResult, Vendor } from "../api/types";

const emptyLine = (): InvoiceLine => ({ description: "", quantity: 1, unitPrice: 0, vatRate: 20 });

export function InvoiceFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const toast = useRef<Toast>(null);
  const [vendors, setVendors] = useState<Vendor[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [direction, setDirection] = useState<InvoiceDirection>("EXPENSE");
  const [status, setStatus] = useState<InvoiceStatus>("PENDING");
  const [vendorId, setVendorId] = useState<string | null>(null);
  const [categoryId, setCategoryId] = useState<string | null>(null);
  const [invoiceNumber, setInvoiceNumber] = useState("");
  const [issueDate, setIssueDate] = useState<Date>(new Date());
  const [dueDate, setDueDate] = useState<Date | null>(null);
  const [currency, setCurrency] = useState("TRY");
  const [paymentMethod, setPaymentMethod] = useState("");
  const [notes, setNotes] = useState("");
  const [subtotal, setSubtotal] = useState<number>(0);
  const [vatAmount, setVatAmount] = useState<number>(0);
  const [total, setTotal] = useState<number>(0);
  const [lines, setLines] = useState<InvoiceLine[]>([emptyLine()]);
  const [warnings, setWarnings] = useState<string[]>([]);

  useEffect(() => {
    api<Vendor[]>("/api/vendors/all").then(setVendors);
    api<Category[]>("/api/categories").then(setCategories);
    if (id) {
      api<InvoiceDetail>(`/api/invoices/${id}`).then((invoice) => {
        setDirection(invoice.direction);
        setStatus(invoice.status);
        setVendorId(invoice.vendorId ?? null);
        setCategoryId(invoice.categoryId ?? null);
        setInvoiceNumber(invoice.invoiceNumber ?? "");
        setIssueDate(new Date(invoice.issueDate));
        setDueDate(invoice.dueDate ? new Date(invoice.dueDate) : null);
        setCurrency(invoice.currency);
        setPaymentMethod(invoice.paymentMethod ?? "");
        setNotes(invoice.notes ?? "");
        setSubtotal(invoice.subtotal);
        setVatAmount(invoice.vatAmount);
        setTotal(invoice.total);
        setLines(invoice.lines.length ? invoice.lines : [emptyLine()]);
      });
    }
  }, [id]);

  const applyOcr = (result: OcrResult) => {
    setWarnings(result.warnings ?? []);
    if (result.invoiceNumber) setInvoiceNumber(result.invoiceNumber);
    if (result.issueDate) setIssueDate(new Date(result.issueDate));
    if (result.dueDate) setDueDate(new Date(result.dueDate));
    if (result.currency) setCurrency(result.currency);
    if (result.subtotal != null) setSubtotal(result.subtotal);
    if (result.vatAmount != null) setVatAmount(result.vatAmount);
    if (result.total != null) setTotal(result.total);
    if (result.vendorName) {
      const match = vendors.find((vendor) => vendor.name.toLowerCase() === result.vendorName?.toLowerCase());
      if (match) setVendorId(match.id);
    }
    toast.current?.show({ severity: "success", summary: "Belge okundu", detail: "Alanları kontrol edin." });
  };

  const readDocument = async (file: File) => {
    const body = new FormData();
    body.append("file", file);
    try {
      applyOcr(await api<OcrResult>("/api/ocr", { method: "POST", body }));
    } catch (err) {
      toast.current?.show({ severity: "error", summary: err instanceof Error ? err.message : "Okuma başarısız" });
    }
  };

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    const payload = {
      direction,
      status,
      vendorId,
      categoryId,
      invoiceNumber,
      issueDate: issueDate.toISOString().slice(0, 10),
      dueDate: dueDate ? dueDate.toISOString().slice(0, 10) : null,
      currency,
      paymentMethod,
      notes,
      subtotal,
      vatAmount,
      total,
      lines,
    };
    try {
      const saved = await api<InvoiceDetail>(id ? `/api/invoices/${id}` : "/api/invoices", {
        method: id ? "PUT" : "POST",
        body: JSON.stringify(payload),
      });
      navigate(`/faturalar/${saved.id}`);
    } catch (err) {
      toast.current?.show({ severity: "error", summary: err instanceof Error ? err.message : "Kayıt başarısız" });
    }
  };

  return (
    <form className="flex flex-column gap-3" onSubmit={submit}>
      <Toast ref={toast} />
      <div className="toolbar">
        <div>
          <h2 className="page-title">{id ? "Faturayı düzenle" : "Yeni fatura"}</h2>
          <p className="page-subtitle">Elle giriş, belge okuma veya UBL sonrası inceleme</p>
        </div>
        <FileUpload mode="basic" auto chooseLabel="PDF / XML oku" customUpload uploadHandler={(e) => readDocument(e.files[0])} />
      </div>
      {warnings.length > 0 && <div className="panel">{warnings.join(" ")}</div>}
      <div className="panel grid">
        <div className="col-12 md:col-3">
          <label>Yön</label>
          <Dropdown value={direction} onChange={(e) => setDirection(e.value)} options={[{ label: "Gider", value: "EXPENSE" }, { label: "Gelir", value: "INCOME" }]} className="w-full" />
        </div>
        <div className="col-12 md:col-3">
          <label>Durum</label>
          <Dropdown value={status} onChange={(e) => setStatus(e.value)} options={[{ label: "Taslak", value: "DRAFT" }, { label: "Bekliyor", value: "PENDING" }, { label: "Ödendi", value: "PAID" }, { label: "Gecikti", value: "OVERDUE" }, { label: "İptal", value: "CANCELLED" }]} className="w-full" />
        </div>
        <div className="col-12 md:col-3">
          <label>Cari</label>
          <Dropdown value={vendorId} onChange={(e) => setVendorId(e.value)} options={vendors} optionLabel="name" optionValue="id" showClear className="w-full" />
        </div>
        <div className="col-12 md:col-3">
          <label>Kategori</label>
          <Dropdown value={categoryId} onChange={(e) => setCategoryId(e.value)} options={categories.filter((c) => c.kind === direction || c.kind === "BOTH")} optionLabel="name" optionValue="id" showClear className="w-full" />
        </div>
        <div className="col-12 md:col-3">
          <label>Fatura no</label>
          <InputText value={invoiceNumber} onChange={(e) => setInvoiceNumber(e.target.value)} className="w-full" />
        </div>
        <div className="col-12 md:col-3">
          <label>Tarih</label>
          <Calendar value={issueDate} onChange={(e) => setIssueDate(e.value as Date)} dateFormat="dd.mm.yy" className="w-full" />
        </div>
        <div className="col-12 md:col-3">
          <label>Vade</label>
          <Calendar value={dueDate} onChange={(e) => setDueDate(e.value as Date)} dateFormat="dd.mm.yy" showButtonBar className="w-full" />
        </div>
        <div className="col-12 md:col-3">
          <label>Ödeme yöntemi</label>
          <InputText value={paymentMethod} onChange={(e) => setPaymentMethod(e.target.value)} className="w-full" />
        </div>
        <div className="col-12 md:col-4">
          <label>Ara toplam</label>
          <InputNumber value={subtotal} onValueChange={(e) => setSubtotal(e.value ?? 0)} mode="currency" currency={currency} locale="tr-TR" className="w-full" />
        </div>
        <div className="col-12 md:col-4">
          <label>KDV</label>
          <InputNumber value={vatAmount} onValueChange={(e) => setVatAmount(e.value ?? 0)} mode="currency" currency={currency} locale="tr-TR" className="w-full" />
        </div>
        <div className="col-12 md:col-4">
          <label>Toplam</label>
          <InputNumber value={total} onValueChange={(e) => setTotal(e.value ?? 0)} mode="currency" currency={currency} locale="tr-TR" className="w-full" />
        </div>
        <div className="col-12">
          <label>Not</label>
          <InputTextarea value={notes} onChange={(e) => setNotes(e.target.value)} rows={3} className="w-full" />
        </div>
      </div>
      <div className="panel">
        <div className="toolbar">
          <h3>Kalemler</h3>
          <Button type="button" label="Kalem ekle" icon="pi pi-plus" text onClick={() => setLines((current) => [...current, emptyLine()])} />
        </div>
        {lines.map((line, index) => (
          <div className="grid" key={index}>
            <div className="col-12 md:col-4">
              <InputText value={line.description} placeholder="Açıklama" onChange={(e) => {
                const next = [...lines];
                next[index] = { ...line, description: e.target.value };
                setLines(next);
              }} className="w-full" />
            </div>
            <div className="col-4 md:col-2">
              <InputNumber value={line.quantity} onValueChange={(e) => {
                const next = [...lines];
                next[index] = { ...line, quantity: e.value ?? 1 };
                setLines(next);
              }} className="w-full" />
            </div>
            <div className="col-4 md:col-3">
              <InputNumber value={line.unitPrice} mode="currency" currency={currency} locale="tr-TR" onValueChange={(e) => {
                const next = [...lines];
                next[index] = { ...line, unitPrice: e.value ?? 0 };
                setLines(next);
              }} className="w-full" />
            </div>
            <div className="col-4 md:col-2">
              <InputNumber value={line.vatRate} suffix="%" onValueChange={(e) => {
                const next = [...lines];
                next[index] = { ...line, vatRate: e.value ?? 0 };
                setLines(next);
              }} className="w-full" />
            </div>
            <div className="col-12 md:col-1">
              <Button type="button" icon="pi pi-trash" text severity="danger" onClick={() => setLines(lines.filter((_, i) => i !== index))} />
            </div>
          </div>
        ))}
      </div>
      <div className="flex justify-content-end">
        <Button type="submit" label="Kaydet" icon="pi pi-check" />
      </div>
    </form>
  );
}
