import { FormEvent, useEffect, useState } from "react";
import { Button } from "primereact/button";
import { Calendar } from "primereact/calendar";
import { Column } from "primereact/column";
import { DataTable } from "primereact/datatable";
import { Dialog } from "primereact/dialog";
import { Dropdown } from "primereact/dropdown";
import { InputNumber } from "primereact/inputnumber";
import { InputText } from "primereact/inputtext";
import { api } from "../api/client";
import type { Category, InvoiceDirection, RecurringInterval, RecurringRule, Vendor } from "../api/types";
import { dateTr, directionLabel, intervalLabel, money } from "../format";

const empty = {
  title: "",
  direction: "EXPENSE" as InvoiceDirection,
  amount: 0,
  interval: "MONTHLY" as RecurringInterval,
  nextDueDate: new Date(),
  vendorId: null as string | null,
  categoryId: null as string | null,
  active: true,
};

export function RecurringPage() {
  const [rows, setRows] = useState<RecurringRule[]>([]);
  const [vendors, setVendors] = useState<Vendor[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [visible, setVisible] = useState(false);
  const [form, setForm] = useState(empty);

  const load = () => api<RecurringRule[]>("/api/recurring").then(setRows);

  useEffect(() => {
    load();
    api<Vendor[]>("/api/vendors/all").then(setVendors);
    api<Category[]>("/api/categories").then(setCategories);
  }, []);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    await api("/api/recurring", {
      method: "POST",
      body: JSON.stringify({
        ...form,
        nextDueDate: form.nextDueDate.toISOString().slice(0, 10),
      }),
    });
    setVisible(false);
    setForm(empty);
    load();
  };

  return (
    <div className="flex flex-column gap-3">
      <div className="toolbar">
        <div>
          <h2 className="page-title">Tekrarlayan faturalar</h2>
          <p className="page-subtitle">Aidat, abonelik ve düzenli giderler</p>
        </div>
        <Button label="Yeni kural" onClick={() => setVisible(true)} />
      </div>
      <div className="panel">
        <DataTable value={rows} emptyMessage="Kural yok.">
          <Column field="title" header="Başlık" />
          <Column field="vendorName" header="Cari" />
          <Column field="direction" header="Yön" body={(row) => directionLabel[row.direction]} />
          <Column field="interval" header="Periyot" body={(row) => intervalLabel[row.interval]} />
          <Column field="nextDueDate" header="Sonraki vade" body={(row) => dateTr(row.nextDueDate)} />
          <Column field="amount" header="Tutar" body={(row) => money(row.amount, row.currency)} />
          <Column
            header=""
            body={(row: RecurringRule) => (
              <Button icon="pi pi-trash" text severity="danger" onClick={async () => { await api(`/api/recurring/${row.id}`, { method: "DELETE" }); load(); }} />
            )}
          />
        </DataTable>
      </div>
      <Dialog header="Tekrarlayan fatura" visible={visible} onHide={() => setVisible(false)}>
        <form className="flex flex-column gap-2" onSubmit={submit}>
          <InputText value={form.title} placeholder="Başlık" onChange={(e) => setForm({ ...form, title: e.target.value })} required />
          <Dropdown value={form.direction} options={[{ label: "Gider", value: "EXPENSE" }, { label: "Gelir", value: "INCOME" }]} onChange={(e) => setForm({ ...form, direction: e.value })} />
          <Dropdown value={form.interval} options={Object.entries(intervalLabel).map(([value, label]) => ({ value, label }))} onChange={(e) => setForm({ ...form, interval: e.value })} />
          <Dropdown value={form.vendorId} options={vendors} optionLabel="name" optionValue="id" placeholder="Cari" showClear onChange={(e) => setForm({ ...form, vendorId: e.value })} />
          <Dropdown value={form.categoryId} options={categories} optionLabel="name" optionValue="id" placeholder="Kategori" showClear onChange={(e) => setForm({ ...form, categoryId: e.value })} />
          <InputNumber value={form.amount} mode="currency" currency="TRY" locale="tr-TR" onValueChange={(e) => setForm({ ...form, amount: e.value ?? 0 })} />
          <Calendar value={form.nextDueDate} dateFormat="dd.mm.yy" onChange={(e) => setForm({ ...form, nextDueDate: e.value as Date })} />
          <Button type="submit" label="Kaydet" />
        </form>
      </Dialog>
    </div>
  );
}
