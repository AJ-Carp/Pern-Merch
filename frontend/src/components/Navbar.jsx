import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';

export default function Navbar() {
  const { user, logoutUser } = useAuth();
  const { cartCount } = useCart();
  const [menuOpen, setMenuOpen] = useState(false);

  const closeMenu = () => setMenuOpen(false);

  const links = (
    <>
      <Link to="/" onClick={closeMenu}>Home</Link>
      <Link to="/products" onClick={closeMenu}>Shop</Link>
      {user ? (
        <>
          <Link to="/cart" className="cart-link" onClick={closeMenu}>
            Cart {cartCount > 0 && <span className="cart-badge">{cartCount}</span>}
          </Link>
          <Link to="/orders" onClick={closeMenu}>Orders</Link>
          {user.role === 'ADMIN' && <Link to="/admin" onClick={closeMenu}>Admin</Link>}
          <span className="navbar-user">Hi, {user.username}</span>
          <button className="btn btn-sm btn-outline" onClick={() => { logoutUser(); closeMenu(); }}>Logout</button>
        </>
      ) : (
        <>
          <Link to="/login" onClick={closeMenu}>Sign In</Link>
          <Link to="/register" className="btn btn-sm btn-primary" onClick={closeMenu}>Sign Up</Link>
        </>
      )}
    </>
  );

  return (
    <nav className="navbar">
      <div className="navbar-inner">
        <Link to="/" className="navbar-brand">PERN</Link>

        {/* Desktop links */}
        <div className="navbar-links navbar-desktop">
          {links}
        </div>

        {/* Hamburger button */}
        <button
          className={`hamburger ${menuOpen ? 'open' : ''}`}
          onClick={() => setMenuOpen(!menuOpen)}
          aria-label="Toggle menu"
        >
          <span />
          <span />
          <span />
        </button>
      </div>

      {/* Mobile sidebar overlay */}
      {menuOpen && <div className="sidebar-overlay" onClick={closeMenu} />}

      {/* Mobile sidebar */}
      <div className={`sidebar ${menuOpen ? 'sidebar-open' : ''}`}>
        <div className="sidebar-links">
          {links}
        </div>
      </div>
    </nav>
  );
}