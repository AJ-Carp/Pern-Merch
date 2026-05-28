import { useEffect, useRef, useState } from 'react';
import { Elements } from '@stripe/react-stripe-js';
import { loadStripe } from '@stripe/stripe-js';
import { createPaymentIntent, getDefaultAddress } from '../api/api';
import CheckoutForm from '../components/CheckoutForm';

const stripePromise = loadStripe(import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY);

function toStripeDefaults(dto) {
  if (!dto) return null;
  return {
    name: dto.recipientName,
    phone: dto.phone,
    address: {
      line1: dto.line1,
      line2: dto.line2 || '',
      city: dto.city,
      state: dto.state,
      postal_code: dto.postalCode,
      country: dto.country,
    },
  };
}

export default function Checkout() {
  const [clientSecret, setClientSecret] = useState(null);
  const [defaultAddress, setDefaultAddress] = useState(null);
  const [ready, setReady] = useState(false);
  const [error, setError] = useState(null);
  const fetchedRef = useRef(false);

  useEffect(() => {
    if (fetchedRef.current) return;
    fetchedRef.current = true;

    Promise.allSettled([createPaymentIntent(), getDefaultAddress()])
      .then(([piResult, addrResult]) => {
        if (piResult.status === 'rejected') {
          setError(piResult.reason?.message || 'Failed to start checkout');
          return;
        }
        setClientSecret(piResult.value.clientSecret);
        if (addrResult.status === 'fulfilled' && addrResult.value) {
          setDefaultAddress(toStripeDefaults(addrResult.value));
        }
        setReady(true);
      });
  }, []);

  if (error) return <div className="section"><p className="error">{error}</p></div>;
  if (!ready) return <p className="loading-text">Loading payment...</p>;

  const options = { clientSecret, appearance: { theme: 'night' } };

  return (
    <div className="section">
      <h2 className="section-title">Checkout</h2>
      <Elements stripe={stripePromise} options={options}>
        <CheckoutForm clientSecret={clientSecret} defaultAddress={defaultAddress} />
      </Elements>
    </div>
  );
}
