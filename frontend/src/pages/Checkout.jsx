import { useEffect, useRef, useState } from 'react';
import { useLocation } from 'react-router-dom';
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
  const location = useLocation();
  // The cart page creates the payment intent and passes the secret in, so a stock
  // rejection stays on the cart. This is null on a direct visit / refresh — then we
  // create it here (the backend reuses the pending order, so it's safe).
  const passedClientSecret = location.state?.clientSecret || null;
  const [clientSecret, setClientSecret] = useState(null);
  const [defaultAddress, setDefaultAddress] = useState(null);
  const [ready, setReady] = useState(false);
  const [error, setError] = useState(null);
  const fetchedRef = useRef(false);

  useEffect(() => {
    if (fetchedRef.current) return;
    fetchedRef.current = true;

    // Only create the intent here when the cart didn't already hand us one.
    const intentPromise = passedClientSecret
      ? Promise.resolve({ clientSecret: passedClientSecret })
      : createPaymentIntent();

    Promise.allSettled([intentPromise, getDefaultAddress()])
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
  }, [passedClientSecret]);

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
