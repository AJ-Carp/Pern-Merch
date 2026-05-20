import { useState } from 'react';
import { PaymentElement, useStripe, useElements } from '@stripe/react-stripe-js';
import { cancelPendingCheckout } from '../api/api';

export default function CheckoutForm({ clientSecret }) {
  const stripe = useStripe();
  const elements = useElements();
  const [submitting, setSubmitting] = useState(false);
  const [cancelling, setCancelling] = useState(false);
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

  async function handleCancel() {
    if (submitting || cancelling) return;
    setCancelling(true);
    setErrorMsg(null);
    try {
      await cancelPendingCheckout();
      window.location.assign(`${window.location.origin}/cart`);
    } catch (err) {
      if (err.alreadyPaid) {
        // The payment went through in the moment between the user clicking
        // Cancel and our server reaching Stripe. Send them to the success page.
        window.location.assign(`${window.location.origin}/checkout/success`);
        return;
      }
      setErrorMsg(err.message);
      setCancelling(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="checkout-form">
      <PaymentElement />
      {errorMsg && <p className="error" style={{ marginTop: 12 }}>{errorMsg}</p>}
      <div style={{ display: 'flex', gap: 12, marginTop: 16 }}>
        <button
          type="submit"
          className="btn btn-primary btn-lg"
          disabled={!stripe || submitting || cancelling}
        >
          {submitting ? 'Processing...' : 'Pay now'}
        </button>
        <button
          type="button"
          onClick={handleCancel}
          className="btn btn-secondary btn-lg"
          disabled={submitting || cancelling}
        >
          {cancelling ? 'Cancelling...' : 'Cancel order'}
        </button>
      </div>
    </form>
  );
}
