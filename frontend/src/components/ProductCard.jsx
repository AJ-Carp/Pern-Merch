import { Link } from 'react-router-dom';

export default function ProductCard({ product }) {
  const variants = product.variants || [];
  // Out of stock only when the product has variants and none of them have stock.
  const outOfStock = variants.length > 0 && variants.every(v => v.stockQuantity === 0);

  return (
    <Link to={`/products/${product.id}`} className="product-card">
      <div className="product-card-img">
        <img src={product.imageUrl || 'https://via.placeholder.com/300x300/1a1a2e/e94560?text=PERN'} alt={product.name} />
      </div>
      <div className="product-card-body">
        <span className="product-card-category">{product.category}</span>
        <h3>{product.name}</h3>
        <p className="product-card-price">${product.price?.toFixed(2)}</p>
        {outOfStock && <span className="out-of-stock-badge">Out of Stock</span>}
      </div>
    </Link>
  );
}
