import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { loadStripe } from '@stripe/stripe-js';
import { useCart } from '../context/CartContext';

const stripePromise = loadStripe(import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY);

export default function CheckoutSuccess() {
  const [params] = useSearchParams();
  const [status, setStatus] = useState('checking');
  const navigate = useNavigate();
  const { clearLocalCart, loadCart } = useCart();

  useEffect(() => {
    const clientSecret = params.get('payment_intent_client_secret');
    if (!clientSecret) {
      setStatus('unknown');
      return;
    }

    stripePromise.then(stripe =>
      stripe.retrievePaymentIntent(clientSecret).then(({ paymentIntent }) => {
        setStatus(paymentIntent.status);
        if (paymentIntent.status === 'succeeded') {
          clearLocalCart();
          loadCart();
        }
      })
    );
  }, []);

  return (
    <div className="section">
      {status === 'succeeded' && (
        <>
          <h2>Payment received!</h2>
          <p>Your order is being confirmed. It will appear in your order history shortly.</p>
          <button className="btn btn-primary" onClick={() => navigate('/orders')}>
            View orders
          </button>
        </>
      )}
      {status === 'processing' && (
        <>
          <h2>Payment processing</h2>
          <p>We'll update you when it completes.</p>
        </>
      )}
      {(status === 'requires_payment_method' || status === 'unknown') && (
        <>
          <h2>Payment didn't go through</h2>
          <button className="btn btn-primary" onClick={() => navigate('/checkout')}>
            Try again
          </button>
        </>
      )}
      {status === 'checking' && <p className="loading-text">Confirming payment...</p>}
    </div>
  );
}
