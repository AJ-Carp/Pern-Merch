import { createContext, useContext, useState, useCallback } from 'react';
import { getCart as fetchCart, addToCart as apiAddToCart, updateCartItem as apiUpdateCartItem, removeCartItem as apiRemoveCartItem } from '../api/api';
import { useAuth } from './AuthContext';

const CartContext = createContext(null);

export function CartProvider({ children }) {
  const [cartItems, setCartItems] = useState([]);
  const [loading, setLoading] = useState(false);
  const { user } = useAuth();

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

  async function addToCart(productId, quantity = 1) {
    await apiAddToCart(productId, quantity);
    await loadCart({ silent: true });
  }

  async function updateQuantity(cartItemId, quantity) {
    await apiUpdateCartItem(cartItemId, quantity);
    await loadCart({ silent: true });
  }

  async function removeItem(cartItemId) {
    await apiRemoveCartItem(cartItemId);
    await loadCart({ silent: true });
  }

  function clearLocalCart() {
    setCartItems([]);
  }

  const cartCount = cartItems.reduce((sum, item) => sum + item.quantity, 0);

  return (
    <CartContext.Provider value={{ cartItems, cartCount, loading, loadCart, addToCart, updateQuantity, removeItem, clearLocalCart }}>
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  return useContext(CartContext);
}
