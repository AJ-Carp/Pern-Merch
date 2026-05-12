import { useEffect, useRef, useState } from 'react';
import { Elements } from '@stripe/react-stripe-js';
import { loadStripe } from '@stripe/stripe-js';
import { createPaymentIntent } from '../api/api';
import CheckoutForm from '../components/CheckoutForm';

const stripePromise = loadStripe(import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY);

export default function Checkout() {
  const [clientSecret, setClientSecret] = useState(null);
  const [error, setError] = useState(null);
  const fetchedRef = useRef(false);

  useEffect(() => {
    if (fetchedRef.current) return;
    fetchedRef.current = true;
    createPaymentIntent()
      .then(data => setClientSecret(data.clientSecret))
      .catch(err => setError(err.message));
  }, []);

  if (error) return <div className="section"><p className="error">{error}</p></div>;
  if (!clientSecret) return <p className="loading-text">Loading payment...</p>;

  const options = { clientSecret, appearance: { theme: 'night' } };

  return (
    <div className="section">
      <h2 className="section-title">Checkout</h2>
      <Elements stripe={stripePromise} options={options}>
        <CheckoutForm clientSecret={clientSecret} />
      </Elements>
    </div>
  );
}
