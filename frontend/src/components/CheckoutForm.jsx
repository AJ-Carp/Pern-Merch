import { useState } from 'react';
import { PaymentElement, useStripe, useElements } from '@stripe/react-stripe-js';

export default function CheckoutForm({ clientSecret }) {
  const stripe = useStripe();
  const elements = useElements();
  const [submitting, setSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState(null);

  async function handleSubmit(e) {
    e.preventDefault();
    if (!stripe || !elements || submitting) return;
    setSubmitting(true);
    setErrorMsg(null);

    const { error } = await stripe.confirmPayment({
      elements,
      confirmParams: {
        return_url: `${window.location.origin}/checkout/success`,
      },
      redirect: 'if_required',
    });

    if (error) {
      setErrorMsg(error.message);
      setSubmitting(false);
      return;
    }

    const successUrl = clientSecret
      ? `${window.location.origin}/checkout/success?payment_intent_client_secret=${encodeURIComponent(clientSecret)}`
      : `${window.location.origin}/checkout/success`;
    window.location.assign(successUrl);
  }

  return (
    <form onSubmit={handleSubmit} className="checkout-form">
      <PaymentElement />
      {errorMsg && <p className="error" style={{ marginTop: 12 }}>{errorMsg}</p>}
      <button
        type="submit"
        className="btn btn-primary btn-lg"
        disabled={!stripe || submitting}
        style={{ marginTop: 16 }}
      >
        {submitting ? 'Processing...' : 'Pay now'}
      </button>
    </form>
  );
}
