import { Link } from 'react-router-dom';

export default function Home() {
  return (
    <div>
      {/* Hero */}
      <section className="hero">
        <video className="hero-video" autoPlay loop muted playsInline>
          <source src="/hero.mp4" type="video/mp4" />
        </video>
        <div className="hero-overlay" />
        <div className="hero-content">
          <h1>PERN</h1>
          <p className="hero-subtitle">Official Merch Store</p>
          <Link to="/products" className="btn btn-shop-now btn-lg">Shop Now</Link>
        </div>
      </section>

      <div className="home-duo-row">
      {/* Music */}
      <section className="section duo-section">
        <h2 className="section-title">Listen To Our Music</h2>
        <div className="socials-grid">
          <a href="https://open.spotify.com/artist/1eXv7iCiSpgbO7l8v4E841" target="_blank" rel="noopener noreferrer" className="social-card">
            <svg viewBox="0 0 24 24" width="32" height="32" fill="currentColor"><path d="M12 0C5.4 0 0 5.4 0 12s5.4 12 12 12 12-5.4 12-12S18.66 0 12 0Zm5.521 17.34c-.24.359-.66.48-1.021.24-2.82-1.74-6.36-2.101-10.561-1.141-.418.122-.779-.179-.899-.539-.12-.421.18-.78.54-.9 4.56-1.021 8.52-.6 11.64 1.32.42.18.479.659.301 1.02Zm1.44-3.3c-.301.42-.841.6-1.262.3-3.239-1.98-8.159-2.58-11.939-1.38-.479.12-1.02-.12-1.14-.6-.12-.48.12-1.021.6-1.141C9.6 9.9 15 10.561 18.72 12.84c.361.181.54.78.241 1.2Zm.12-3.36C15.24 8.4 8.82 8.16 5.16 9.301c-.6.179-1.2-.181-1.38-.721-.18-.601.18-1.2.72-1.381 4.26-1.26 11.28-1.02 15.721 1.621.539.3.719 1.02.419 1.56-.299.421-1.02.599-1.559.3Z"/></svg>
            <span>Spotify</span>
          </a>
          <a href="https://music.apple.com/us/artist/pern/1713103921" target="_blank" rel="noopener noreferrer" className="social-card">
            <svg viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"><path d="M9 18V5l12-2v13"/><circle cx="6" cy="18" r="3"/><circle cx="18" cy="16" r="3"/></svg>
            <span>Apple Music</span>
          </a>
        </div>
      </section>

      {/* Socials */}
      <section className="section duo-section">
        <h2 className="section-title">Check Us Out On Socials</h2>
        <div className="socials-grid socials-triangle">
          <a href="https://www.instagram.com/pernbandnj/?hl=en" target="_blank" rel="noopener noreferrer" className="social-card">
            <svg viewBox="0 0 24 24" width="32" height="32" fill="currentColor"><path d="M7.75 2h8.5A5.75 5.75 0 0 1 22 7.75v8.5A5.75 5.75 0 0 1 16.25 22h-8.5A5.75 5.75 0 0 1 2 16.25v-8.5A5.75 5.75 0 0 1 7.75 2Zm0 1.5A4.25 4.25 0 0 0 3.5 7.75v8.5A4.25 4.25 0 0 0 7.75 20.5h8.5a4.25 4.25 0 0 0 4.25-4.25v-8.5A4.25 4.25 0 0 0 16.25 3.5h-8.5ZM12 7a5 5 0 1 1 0 10 5 5 0 0 1 0-10Zm0 1.5a3.5 3.5 0 1 0 0 7 3.5 3.5 0 0 0 0-7Zm5.25-2.5a1 1 0 1 1 0 2 1 1 0 0 1 0-2Z"/></svg>
            <span>Instagram</span>
          </a>
          <a href="https://www.youtube.com/watch?v=dmEeGJRcIg8" target="_blank" rel="noopener noreferrer" className="social-card">
            <svg viewBox="0 0 24 24" width="32" height="32" fill="currentColor"><path d="M23.498 6.186a2.994 2.994 0 0 0-2.106-2.12C19.505 3.546 12 3.546 12 3.546s-7.505 0-9.392.52A2.994 2.994 0 0 0 .502 6.186C0 8.07 0 12 0 12s0 3.93.502 5.814a2.994 2.994 0 0 0 2.106 2.12c1.887.52 9.392.52 9.392.52s7.505 0 9.392-.52a2.994 2.994 0 0 0 2.106-2.12C24 15.93 24 12 24 12s0-3.93-.502-5.814ZM9.545 15.568V8.432L15.818 12l-6.273 3.568Z"/></svg>
            <span>YouTube</span>
          </a>
          <a href="https://www.tiktok.com/@pernband/video/7552321314556579085" target="_blank" rel="noopener noreferrer" className="social-card">
            <svg viewBox="0 0 24 24" width="32" height="32" fill="currentColor"><path d="M12.525.02c1.31-.02 2.61-.01 3.91-.02.08 1.53.63 3.09 1.75 4.17 1.12 1.11 2.7 1.62 4.24 1.79v4.03c-1.44-.05-2.89-.35-4.2-.97-.57-.26-1.1-.59-1.62-.93-.01 2.92.01 5.84-.02 8.75-.08 1.4-.54 2.79-1.35 3.94-1.31 1.92-3.58 3.17-5.91 3.21-1.43.08-2.86-.31-4.08-1.03-2.02-1.19-3.44-3.37-3.65-5.71-.02-.5-.03-1-.01-1.49.18-1.9 1.12-3.72 2.58-4.96 1.66-1.44 3.98-2.13 6.15-1.72.02 1.48-.04 2.96-.04 4.44-.99-.32-2.15-.23-3.02.37-.63.41-1.11 1.04-1.36 1.75-.21.51-.15 1.07-.14 1.61.24 1.64 1.82 3.02 3.5 2.87 1.12-.01 2.19-.66 2.77-1.61.19-.33.4-.67.41-1.06.1-1.79.06-3.57.07-5.36.01-4.03-.01-8.05.02-12.07Z"/></svg>
            <span>TikTok</span>
          </a>
        </div>
      </section>
      </div>

      <a href="#perncast" className="scroll-arrow" aria-label="Scroll to PernCast">
        <svg viewBox="0 0 24 24" width="60" height="60" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="6 9 12 15 18 9" /></svg>
      </a>

      <div className="perncast-spacer" />

      {/* PernCast */}
      <section id="perncast" className="section perncast-section">
        <h2 className="perncast-title">The PERNcast</h2>
        <p className="perncast-description">
          The PernCast is where we hang out with awesome local bands, swap wild stories about band life, and let our guests tear it up with live performances right on the show!
        </p>
        <div className="perncast-video">
          <iframe
            src="https://www.youtube.com/embed/T1qOaDZvpTE"
            title="The PernCast"
            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
            allowFullScreen
          />
        </div>
      </section>
      <div className="perncast-spacer" />
    </div>
  );
}
