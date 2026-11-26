
import { useCallback, useEffect, useState } from "react";
import { Plus, Pencil, Trash2, RefreshCw, ImagePlus } from "lucide-react";
import { sellerApi } from "../api";
import { useToast } from "../../context.jsx";
import "../seller.css";

const EMPTY_FORM = {
  name: "",
  description: "",
  price: "",
  veg: true,
  category: "",
  bestseller: false,
  inStock: true,
  imageUrl: "",
  emoji: "🍽️",
};

export default function MenuPage() {
  const toast = useToast();
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [imageFile, setImageFile] = useState(null);

  const loadMenu = useCallback(async () => {
    try {
      const data = await sellerApi.getMenu();
      setItems(Array.isArray(data) ? data : []);
    } catch (error) {
      toast(error.message || "Unable to load menu", "error");
    } finally {
      setLoading(false);
    }
  }, [toast]);

  useEffect(() => {
    loadMenu();
  }, [loadMenu]);

  function updateField(event) {
    const { name, value, type, checked } = event.target;
    setForm((current) => ({
      ...current,
      [name]: type === "checkbox" ? checked : value,
    }));
  }

  function startEdit(item) {
    setEditingId(item.id);
    setForm({
      name: item.name || "",
      description: item.description || "",
      price: String(item.price ?? ""),
      veg: Boolean(item.veg),
      category: item.category || "",
      bestseller: Boolean(item.bestseller),
      inStock: Boolean(item.inStock),
      imageUrl: item.imageUrl || "",
      emoji: item.emoji || "🍽️",
    });
    setImageFile(null);
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  function resetForm() {
    setEditingId(null);
    setForm(EMPTY_FORM);
    setImageFile(null);
  }

  async function handleSubmit(event) {
    event.preventDefault();

    if (!form.name.trim() || !form.category.trim()) {
      toast("Enter the dish name and category.", "error");
      return;
    }

    const price = Number(form.price);
    if (!Number.isFinite(price) || price < 0) {
      toast("Enter a valid non-negative price.", "error");
      return;
    }

    setSaving(true);
    try {
      let imageUrl = form.imageUrl;

      if (imageFile) {
        const uploaded = await sellerApi.uploadImage(imageFile);
        imageUrl = uploaded.url;
      }

      const payload = {
        ...form,
        name: form.name.trim(),
        description: form.description.trim(),
        category: form.category.trim(),
        price: Math.round(price),
        imageUrl,
      };

      if (editingId) {
        await sellerApi.updateMenuItem(editingId, payload);
        toast("Menu item updated successfully", "success");
      } else {
        await sellerApi.addMenuItem(payload);
        toast("Menu item added successfully", "success");
      }

      resetForm();
      await loadMenu();
    } catch (error) {
      toast(error.message || "Unable to save menu item", "error");
    } finally {
      setSaving(false);
    }
  }

  async function toggleStock(item) {
    try {
      await sellerApi.setStock(item.id, !item.inStock);
      toast(
        item.inStock ? "Item marked out of stock" : "Item is back in stock",
        "success"
      );
      await loadMenu();
    } catch (error) {
      toast(error.message || "Unable to update stock", "error");
    }
  }

  async function deleteItem(item) {
    if (!window.confirm(`Delete "${item.name}" from your menu?`)) return;

    try {
      await sellerApi.deleteMenuItem(item.id);
      toast("Menu item deleted", "success");
      await loadMenu();
      if (editingId === item.id) resetForm();
    } catch (error) {
      toast(error.message || "Unable to delete menu item", "error");
    }
  }

  return (
    <main className="seller-menu-page">
      <header className="seller-page-heading">
        <div>
          <h1>Menu Management</h1>
          <p className="seller-muted">
            Add dishes, update prices and manage availability.
          </p>
        </div>
        <button
          type="button"
          className="seller-btn seller-btn-dark"
          onClick={loadMenu}
          disabled={loading}
        >
          <RefreshCw size={16} /> Refresh
        </button>
      </header>

      <section className="seller-menu-form-card">
        <h2>{editingId ? "Edit Menu Item" : "Add New Dish"}</h2>
        <form onSubmit={handleSubmit} className="seller-menu-form">
          <div className="seller-form-grid">
            <label>
              Dish name *
              <input name="name" value={form.name} onChange={updateField}
                placeholder="e.g. Paneer Butter Masala" maxLength={150} required />
            </label>

            <label>
              Category *
              <input name="category" value={form.category} onChange={updateField}
                placeholder="e.g. Main Course" required />
            </label>

            <label>
              Price (₹) *
              <input name="price" type="number" min="0" step="1"
                value={form.price} onChange={updateField} required />
            </label>

            <label>
              Emoji
              <input name="emoji" value={form.emoji} onChange={updateField}
                maxLength={20} />
            </label>
          </div>

          <label>
            Description
            <textarea name="description" value={form.description}
              onChange={updateField} maxLength={500} rows={3}
              placeholder="Describe the dish and its ingredients..." />
          </label>

          <label className="seller-image-upload">
            <ImagePlus size={18} />
            <span>{imageFile ? imageFile.name : "Choose dish image (optional)"}</span>
            <input type="file" accept="image/jpeg,image/png"
              onChange={(event) => setImageFile(event.target.files?.[0] || null)} />
          </label>

          {form.imageUrl && !imageFile && (
            <p className="seller-muted">An image is currently assigned to this dish.</p>
          )}

          <div className="seller-menu-checkboxes">
            <label><input type="checkbox" name="veg"
              checked={form.veg} onChange={updateField} /> Vegetarian</label>
            <label><input type="checkbox" name="bestseller"
              checked={form.bestseller} onChange={updateField} /> Bestseller</label>
            <label><input type="checkbox" name="inStock"
              checked={form.inStock} onChange={updateField} /> In stock</label>
          </div>

          <div className="seller-menu-form-actions">
            <button type="submit" className="seller-btn seller-btn-primary"
              disabled={saving}>
              {saving ? "Saving..." : editingId ? "Save Changes" : (
                <><Plus size={17} /> Add Dish</>
              )}
            </button>
            {editingId && (
              <button type="button" className="seller-btn seller-btn-dark"
                onClick={resetForm} disabled={saving}>
                Cancel Edit
              </button>
            )}
          </div>
        </form>
      </section>

      <section className="seller-menu-list-section">
        <h2>Your Menu ({items.length})</h2>

        {loading ? (
          <p className="seller-muted">Loading menu...</p>
        ) : items.length === 0 ? (
          <div className="seller-menu-empty">
            <span>🍽️</span>
            <strong>No dishes added yet</strong>
            <p>Add your first dish using the form above.</p>
          </div>
        ) : (
          <div className="seller-menu-grid">
            {items.map((item) => (
              <article className="seller-menu-card" key={item.id}>
                <div className="seller-menu-card-image">
                  {item.imageUrl ? (
                    <img src={item.imageUrl} alt={item.name} />
                  ) : (
                    <span>{item.emoji || "🍽️"}</span>
                  )}
                  <span className={`seller-stock-label ${item.inStock ? "available" : "unavailable"}`}>
                    {item.inStock ? "In Stock" : "Out of Stock"}
                  </span>
                </div>
                <div className="seller-menu-card-body">
                  <div className="seller-menu-card-title">
                    <h3>{item.name}</h3>
                    <span>{item.veg ? "🟢 Veg" : "🔴 Non-veg"}</span>
                  </div>
                  <p className="seller-muted">{item.category}</p>
                  {item.description && <p>{item.description}</p>}
                  <strong className="seller-menu-price">
                    ₹{Number(item.price || 0).toLocaleString("en-IN")}
                  </strong>
                  {item.bestseller && <span className="seller-bestseller">Bestseller</span>}
                  <div className="seller-menu-card-actions">
                    <button type="button" className="seller-btn seller-btn-dark"
                      onClick={() => startEdit(item)}>
                      <Pencil size={15} /> Edit
                    </button>
                    <button type="button" className="seller-btn seller-btn-dark"
                      onClick={() => toggleStock(item)}>
                      {item.inStock ? "Mark Unavailable" : "Mark Available"}
                    </button>
                    <button type="button" className="seller-btn seller-btn-danger"
                      onClick={() => deleteItem(item)}>
                      <Trash2 size={15} /> Delete
                    </button>
                  </div>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </main>
  );
}
