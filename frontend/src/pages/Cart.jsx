import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { checkout } from '../api/api';

export default function Cart() {
  const { cartItems, cartCount, loading, loadCart, updateQuantity, removeItem, clearLocalCart } = useCart();
  const [checkingOut, setCheckingOut] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    loadCart();
  }, [loadCart]);

  const total = cartItems.reduce((sum, item) => sum + item.productPrice * item.quantity, 0);

  async function handleUpdateQuantity(cartItemId, quantity) {
    try {
      await updateQuantity(cartItemId, quantity);
    } catch (err) {
      alert(err.message);
    }
  }

  async function handleCheckout() {
    setCheckingOut(true);
    try {
      await checkout();
      clearLocalCart();
      alert('Order placed successfully!');
      navigate('/orders');
    } catch (err) {
      alert(err.message);
    } finally {
      setCheckingOut(false);
    }
  }

  if (loading) return <p className="loading-text">Loading cart...</p>;

  return (
    <div className="section">
      <h2 className="section-title">Your Cart ({cartCount} items)</h2>

      {cartItems.length === 0 ? (
        <div className="empty-state">
          <p>Your cart is empty.</p>
          <button className="btn btn-primary" onClick={() => navigate('/products')}>Browse Merch</button>
        </div>
      ) : (
        <>
          <div className="cart-list">
            {cartItems.map(item => (
              <div key={item.id} className="cart-item">
                <img src={item.productImageUrl || 'https://via.placeholder.com/80x80/1a1a2e/e94560?text=P'} alt={item.productName} className="cart-item-img" />
                <div className="cart-item-info">
                  <h3>{item.productName}</h3>
                  {item.size && item.size !== 'ONE_SIZE' && <span className="cart-item-size">Size: {item.size}</span>}
                  <span className="cart-item-price">${item.productPrice?.toFixed(2)}</span>
                </div>
                <div className="cart-item-actions">
                  <button className="qty-btn" onClick={() => handleUpdateQuantity(item.id, item.quantity - 1)}>-</button>
                  <span className="qty-value">{item.quantity}</span>
                  <button className="qty-btn" onClick={() => handleUpdateQuantity(item.id, item.quantity + 1)} disabled={item.quantity >= item.stockQuantity}>+</button>
                  <button className="btn btn-danger btn-sm" onClick={() => removeItem(item.id)}>Remove</button>
                </div>
              </div>
            ))}
          </div>

          <div className="cart-summary">
            <div className="cart-total">
              <span>Total:</span>
              <span className="cart-total-price">${total.toFixed(2)}</span>
            </div>
            <button className="btn btn-primary btn-lg" onClick={handleCheckout} disabled={checkingOut}>
              {checkingOut ? 'Processing...' : 'Checkout'}
            </button>
          </div>
        </>
      )}
    </div>
  );
}
