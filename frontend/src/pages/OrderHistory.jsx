import { useEffect, useState } from 'react';
import { getOrders } from '../api/api';

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
                  <span className={`order-status status-${order.status.toLowerCase()}`}>{order.status}</span>
                  <span className="order-total">${order.totalAmount?.toFixed(2)}</span>
                </div>
              </div>
              <div className="order-items">
                {order.items.map((item, i) => (
                  <div key={i} className="order-item">
                    <span>{item.productName}</span>
                    <span>x{item.quantity}</span>
                    <span>${item.priceAtPurchase?.toFixed(2)}</span>
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
