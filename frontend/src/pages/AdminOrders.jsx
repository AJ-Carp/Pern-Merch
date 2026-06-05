import { useEffect, useState } from 'react';
import { getWorkingOrders, getShippedOrders, updateOrderStatus } from '../api/api';
import { isOneSize } from '../utils/size';

const STATUS_LABELS = {
  PAID: 'Paid',
  CONFIRMED: 'Confirmed',
  SHIPPED: 'Shipped',
  DELIVERED: 'Delivered',
  CANCELLED: 'Cancelled',
};

// The next step in the fulfillment ladder, and the button that triggers it.
// PAID → CONFIRMED → SHIPPED. SHIPPED has no further admin-driven step.
const NEXT_STEP = {
  PAID: { status: 'CONFIRMED', label: 'Mark Confirmed' },
  CONFIRMED: { status: 'SHIPPED', label: 'Mark Shipped' },
};

function formatDate(value) {
  if (!value) return '—';
  return new Date(value).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
}

function itemCount(order) {
  return (order.items || []).reduce((sum, item) => sum + item.quantity, 0);
}

function customerName(order) {
  return order.shippingAddress?.recipientName || '—';
}

function StatusBadge({ status }) {
  return (
    <span className={`order-status status-${status.toLowerCase()}`}>
      {STATUS_LABELS[status] || status}
    </span>
  );
}

// Expanded line items + shipping address for one order. Rendered inside a full-width cell.
function OrderDetail({ order }) {
  return (
    <div className="admin-order-detail" style={{ padding: '4px 2px' }}>
      <div className="order-items">
        {(order.items || []).map((item, i) => (
          <div key={i} className="order-item">
            <span>{item.productName}{!isOneSize(item.size) ? ` — ${item.size}` : ''}</span>
            <span>x{item.quantity}</span>
            <span>${item.priceAtPurchase?.toFixed(2)}</span>
          </div>
        ))}
      </div>
      {order.shippingAddress && (
        <div className="order-shipping" style={{ marginTop: 12, fontSize: '0.9em', opacity: 0.85 }}>
          <strong>Ship to:</strong>
          <div>{order.shippingAddress.recipientName}</div>
          <div>
            {order.shippingAddress.line1}
            {order.shippingAddress.line2 ? `, ${order.shippingAddress.line2}` : ''}
          </div>
          <div>
            {order.shippingAddress.city}, {order.shippingAddress.state} {order.shippingAddress.postalCode}
          </div>
          <div>{order.shippingAddress.country}</div>
          <div>{order.shippingAddress.phone}</div>
        </div>
      )}
    </div>
  );
}

// ── Open Orders: the work queue (PAID + CONFIRMED), oldest-first, with advance-status. ──
export function OpenOrders() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [expandedId, setExpandedId] = useState(null);
  const [busyId, setBusyId] = useState(null);

  useEffect(() => { load(); }, []);

  async function load() {
    setLoading(true);
    try {
      setOrders(await getWorkingOrders());
      setError('');
    } catch (err) {
      setError(err.message);
      setOrders([]);
    }
    setLoading(false);
  }

  async function advance(order) {
    const next = NEXT_STEP[order.status];
    if (!next) return;
    setBusyId(order.id);
    setError('');
    try {
      await updateOrderStatus(order.id, next.status);
      await load();
    } catch (err) {
      setError(err.message);
    }
    setBusyId(null);
  }

  if (loading) return <p className="loading-text">Loading orders...</p>;

  return (
    <>
      {error && <p className="error-msg">{error}</p>}
      {orders.length === 0 ? (
        <div className="empty-state"><p>No open orders. All caught up. 🎉</p></div>
      ) : (
        <div className="admin-table-wrapper">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Order #</th>
                <th>Date</th>
                <th>Customer</th>
                <th>Items</th>
                <th>Total</th>
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {orders.map(order => {
                const next = NEXT_STEP[order.status];
                return (
                  <FragmentRow
                    key={order.id}
                    order={order}
                    colSpan={7}
                    expanded={expandedId === order.id}
                    onToggle={() => setExpandedId(prev => (prev === order.id ? null : order.id))}
                  >
                    <td>#{order.id}</td>
                    <td>{formatDate(order.orderDate)}</td>
                    <td>{customerName(order)}</td>
                    <td>{itemCount(order)}</td>
                    <td>${order.totalAmount?.toFixed(2)}</td>
                    <td><StatusBadge status={order.status} /></td>
                    <td>
                      {next && (
                        <button
                          className="btn btn-sm btn-primary"
                          disabled={busyId === order.id}
                          onClick={e => { e.stopPropagation(); advance(order); }}
                        >
                          {busyId === order.id ? '…' : next.label}
                        </button>
                      )}
                    </td>
                  </FragmentRow>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </>
  );
}

// ── Shipped Orders: SHIPPED history, newest-first, paginated, read-only. ──
export function ShippedOrders() {
  const [orders, setOrders] = useState([]);
  const [pageInfo, setPageInfo] = useState({ number: 0, totalPages: 0, totalElements: 0 });
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [expandedId, setExpandedId] = useState(null);

  useEffect(() => { load(page); }, [page]);

  async function load(p) {
    setLoading(true);
    try {
      const data = await getShippedOrders(p);
      setOrders(data.content || []);
      setPageInfo(data.page || { number: p, totalPages: 0, totalElements: 0 });
      setError('');
    } catch (err) {
      setError(err.message);
      setOrders([]);
    }
    setLoading(false);
  }

  if (loading) return <p className="loading-text">Loading orders...</p>;

  const totalPages = pageInfo.totalPages || 0;

  return (
    <>
      {error && <p className="error-msg">{error}</p>}
      {orders.length === 0 ? (
        <div className="empty-state"><p>No shipped orders yet.</p></div>
      ) : (
        <>
          <div className="admin-table-wrapper">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Order #</th>
                  <th>Date</th>
                  <th>Customer</th>
                  <th>Items</th>
                  <th>Total</th>
                </tr>
              </thead>
              <tbody>
                {orders.map(order => (
                  <FragmentRow
                    key={order.id}
                    order={order}
                    colSpan={5}
                    expanded={expandedId === order.id}
                    onToggle={() => setExpandedId(prev => (prev === order.id ? null : order.id))}
                  >
                    <td>#{order.id}</td>
                    <td>{formatDate(order.orderDate)}</td>
                    <td>{customerName(order)}</td>
                    <td>{itemCount(order)}</td>
                    <td>${order.totalAmount?.toFixed(2)}</td>
                  </FragmentRow>
                ))}
              </tbody>
            </table>
          </div>

          <div className="admin-pager" style={{ display: 'flex', alignItems: 'center', gap: 12, marginTop: 16 }}>
            <button
              className="btn btn-sm btn-outline"
              disabled={page <= 0}
              onClick={() => setPage(p => Math.max(0, p - 1))}
            >
              ‹ Prev
            </button>
            <span style={{ fontSize: '0.85em', opacity: 0.7 }}>
              Page {(pageInfo.number ?? 0) + 1}{totalPages ? ` of ${totalPages}` : ''}
            </span>
            <button
              className="btn btn-sm btn-outline"
              disabled={totalPages ? page >= totalPages - 1 : orders.length === 0}
              onClick={() => setPage(p => p + 1)}
            >
              Next ›
            </button>
          </div>
        </>
      )}
    </>
  );
}

// A clickable order row plus, when expanded, a full-width detail row beneath it.
// `children` are the <td> cells of the main row.
function FragmentRow({ order, colSpan, expanded, onToggle, children }) {
  return (
    <>
      <tr onClick={onToggle} style={{ cursor: 'pointer' }}>
        {children}
      </tr>
      {expanded && (
        <tr>
          <td colSpan={colSpan} style={{ background: 'rgba(255,255,255,0.02)' }}>
            <OrderDetail order={order} />
          </td>
        </tr>
      )}
    </>
  );
}
