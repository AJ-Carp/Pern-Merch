import { useState } from 'react';
import AdminProducts from './AdminProducts';
import { OpenOrders, ShippedOrders } from './AdminOrders';

const TABS = [
  { key: 'products', label: 'Products' },
  { key: 'open', label: 'Open Orders' },
  { key: 'shipped', label: 'Shipped Orders' },
];

export default function Admin() {
  const [tab, setTab] = useState('products');

  return (
    <div className="section">
      <h2 className="section-title">Admin Panel</h2>

      <div className="category-tabs">
        {TABS.map(t => (
          <button
            key={t.key}
            className={`tab ${tab === t.key ? 'active' : ''}`}
            onClick={() => setTab(t.key)}
          >
            {t.label}
          </button>
        ))}
      </div>

      {tab === 'products' && <AdminProducts />}
      {tab === 'open' && <OpenOrders />}
      {tab === 'shipped' && <ShippedOrders />}
    </div>
  );
}
