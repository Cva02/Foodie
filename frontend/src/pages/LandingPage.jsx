
import { Link } from "react-router-dom";
import FoodieBrand from "../components/FoodieBrand.jsx";
import "./LandingPage.css";

const portalCards = [
  {
    number: "01",
    title: "Explore delicious food",
    description:
      "Discover nearby restaurants, explore menus, and find something you'll love.",
    action: "Explore restaurants",
    href: "/customer",
    secondary: "Browse as a guest",
    secondaryHref: "/customer",
    className: "foodie-landing-customer",
  },
  {
    number: "02",
    title: "Grow your restaurant",
    description:
      "Reach more customers, manage orders, and grow your business with Foodie.",
    action: "Seller login",
    href: "/seller/login",
    secondary: "Register your restaurant",
    secondaryHref: "/seller/register",
    className: "foodie-landing-seller",
  },
  {
    number: "03",
    title: "Deliver with us",
    description:
      "Join our delivery team, receive delivery requests, and earn with Foodie.",
    action: "Rider login",
    href: "/rider",
    secondary: "Register as a rider",
    secondaryHref: "/rider",
    className: "foodie-landing-rider",
  },
];

export default function LandingPage() {
  return (
    <div className="foodie-landing">
      <header className="foodie-landing-header">
        <Link to="/" className="foodie-landing-logo" aria-label="Foodie home">
          <FoodieBrand logoSize={43} />
        </Link>

        <nav className="foodie-landing-nav" aria-label="Main navigation">
          <a href="#about">About</a>
          <a href="#portals">Our community</a>
          <Link to="/admin/login" className="foodie-landing-admin">
            Admin Login <span aria-hidden="true">↗</span>
          </Link>
        </nav>
      </header>

      <main>
        <section className="foodie-landing-hero">
          <div className="foodie-landing-hero-copy">
            <span className="foodie-landing-eyebrow">
              A GOOD RESTAURANT JUST MAKES YOU HAPPY.
            </span>

            <h1>
              Good food.
              <br />
              Great moments.
              <br />
              <span>Delivered.</span>
            </h1>

            <p>
              Your favourite meals, the restaurants you love, and the people
              who bring it all together. Everything starts here.
            </p>

            <div className="foodie-landing-hero-actions">
              <Link to="/customer" className="foodie-landing-primary">
                Explore food <span aria-hidden="true">→</span>
              </Link>
              <a href="#portals" className="foodie-landing-secondary">
                Discover Foodie
              </a>
            </div>

            <div className="foodie-landing-trust">
              <span className="foodie-landing-trust-dot" />
              <span>Restaurants, customers and delivery partners — together.</span>
            </div>
          </div>

          <div className="foodie-landing-hero-visual" aria-label="Delicious food">
            <div className="foodie-landing-image-frame">
              <img
                src="https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=1100&q=90"
                alt="A table filled with delicious dishes"
                fetchPriority="high"
              />
              <div className="foodie-landing-image-shade" />
              <div className="foodie-landing-image-caption">
                <span>MADE FOR YOUR CRAVINGS</span>
                <strong>Something delicious is waiting.</strong>
              </div>
            </div>

            
            <span className="foodie-landing-orbit" />
          </div>

          <div className="foodie-landing-scroll">
            <span />
            SCROLL TO EXPLORE
          </div>
        </section>

        <section className="foodie-landing-portals" id="portals">
          <div className="foodie-landing-section-heading">
            <span className="foodie-landing-eyebrow">ONE PLATFORM, MANY POSSIBILITIES</span>
            <h2>There's a place for <span>everyone.</span></h2>
            <p>
              Whether you're here for a great meal or to grow your business,
              you're welcome at Foodie.
            </p>
          </div>

          <div className="foodie-landing-card-grid">
            {portalCards.map((card) => (
              <article
                className={`foodie-landing-card ${card.className}`}
                key={card.number}
              >
                <div className="foodie-landing-card-top">
                  <span>{card.number}</span>
                  <span className="foodie-landing-card-symbol">↗</span>
                </div>

                <h3>{card.title}</h3>
                <p>{card.description}</p>

                <div className="foodie-landing-card-actions">
                  <Link to={card.href} className="foodie-landing-card-primary">
                    {card.action} <span aria-hidden="true">→</span>
                  </Link>
                  <Link to={card.secondaryHref} className="foodie-landing-card-secondary">
                    {card.secondary}
                  </Link>
                </div>
              </article>
            ))}
          </div>
        </section>

        <section className="foodie-landing-about" id="about">
          <span className="foodie-landing-eyebrow">THE FOODIE PROMISE</span>
          <h2>
            Bringing good food
            <br />
            and good people <span>together.</span>
          </h2>
          <p>
            Foodie connects customers, restaurants, and delivery partners
            through one simple experience. Discover food, support local
            kitchens, and deliver happiness.
          </p>
          <Link to="/customer" className="foodie-landing-about-link">
            Start exploring <span aria-hidden="true">→</span>
          </Link>
        </section>
      </main>

      <footer className="foodie-landing-footer">
        <FoodieBrand logoSize={34} />
        <span>Good food. Great moments. Delivered.</span>
        <span>© {new Date().getFullYear()} Foodie</span>
      </footer>
    </div>
  );
}
