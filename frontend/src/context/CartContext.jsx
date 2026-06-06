import { createContext, useContext, useState, useCallback, useRef } from 'react';
import { getCart as fetchCart, addToCart as apiAddToCart, updateCartItem as apiUpdateCartItem, removeCartItem as apiRemoveCartItem } from '../api/api';
import { useAuth } from './AuthContext';

const CartContext = createContext(null);

export function CartProvider({ children }) {
  const [cartItems, setCartItems] = useState([]);
  const [loading, setLoading] = useState(false);
  const { user } = useAuth();
  // Per-item debounce timers + latest intended quantity, so a flurry of +/- clicks
  // updates the UI instantly but collapses into a single server sync.
  const syncTimers = useRef({});
  const pendingQty = useRef({});

  // Pass { silent: true } to refresh the cart without flipping the loading
  // flag — this avoids the whole cart unmounting/flashing after a mutation.
  const loadCart = useCallback(async ({ silent = false } = {}) => {
    if (!user) { setCartItems([]); return; }
    if (!silent) setLoading(true);
    try {
      const items = await fetchCart();
      setCartItems(items);
    } catch {
      setCartItems([]);
    } finally {
      if (!silent) setLoading(false);
    }
  }, [user]);

  async function addToCart(productVariantId, quantity = 1) {
    await apiAddToCart(productVariantId, quantity);
    await loadCart({ silent: true });
  }

  // Optimistic, debounced quantity change. The displayed number updates immediately
  // (clamped to [1, stock]); the server sync is deferred so rapid clicks fire once.
  // On rejection we roll back to the server's truth and surface the error.
  function changeItemQuantity(cartItemId, delta) {
    setCartItems(prev => prev.map(item => {
      if (item.id !== cartItemId) return item;
      const next = Math.max(1, Math.min(item.quantity + delta, item.stockQuantity));
      pendingQty.current[cartItemId] = next;
      return { ...item, quantity: next };
    }));

    clearTimeout(syncTimers.current[cartItemId]);
    syncTimers.current[cartItemId] = setTimeout(async () => {
      try {
        await apiUpdateCartItem(cartItemId, pendingQty.current[cartItemId]);
      } catch (err) {
        await loadCart({ silent: true });
        alert(err.message);
      }
    }, 450);
  }

  async function removeItem(cartItemId) {
    // Cancel any pending quantity sync so it can't fire after the item is gone.
    clearTimeout(syncTimers.current[cartItemId]);
    await apiRemoveCartItem(cartItemId);
    await loadCart({ silent: true });
  }

  function clearLocalCart() {
    setCartItems([]);
  }

  const cartCount = cartItems.reduce((sum, item) => sum + item.quantity, 0);

  return (
    <CartContext.Provider value={{ cartItems, cartCount, loading, loadCart, addToCart, changeItemQuantity, removeItem, clearLocalCart }}>
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  return useContext(CartContext);
}
