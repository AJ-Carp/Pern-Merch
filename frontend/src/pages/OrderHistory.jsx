import { useEffect, useState } from 'react';
import { getOrders } from '../api/api';
import { isOneSize } from '../utils/size';

const STATUS_LABELS = {
  PAID: 'Paid',
  CONFIRMED: 'Confirmed',
  SHIPPED: 'Shipped',
  DELIVERED: 'Delivered',
  CANCELLED: 'Cancelled',
};

export default function OrderHistory() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getOrders()
      .then(setOrders)
      .catch(() => setOrders([]))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p className="loading-text">Loading orders...</p>;

  return (
    <div className="section">
      <h2 className="section-title">Order History</h2>

      {orders.length === 0 ? (
        <div className="empty-state">
          <p>No orders yet.</p>
        </div>
      ) : (
        <div className="orders-list">
          {orders.map(order => (
            <div key={order.id} className="order-card">
              <div className="order-header">
                <div>
                  <span className="order-id">Order #{order.id}</span>
                  <span className="order-date">{new Date(order.orderDate).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })}</span>
                </div>
                <div>
                  <span className={`order-status status-${order.status.toLowerCase()}`}>
                    {STATUS_LABELS[order.status] || order.status}
                  </span>
                  <span className="order-total">${order.totalAmount?.toFixed(2)}</span>
                </div>
              </div>
              <div className="order-items">
                {order.items.map((item, i) => (
                  <div key={i} className="order-item">
                    <span>{item.productName}{!isOneSize(item.size) ? ` — ${item.size}` : ''}</span>
                    <span>x{item.quantity}</span>
                    <span>${item.priceAtPurchase?.toFixed(2)}</span>
                  </div>
                ))}
              </div>
              {order.shippingAddress && (
                <div className="order-shipping" style={{ marginTop: 12, fontSize: '0.9em', opacity: 0.85 }}>
                  <strong>Shipped to:</strong>
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
          ))}
        </div>
      )}
    </div>
  );
}
