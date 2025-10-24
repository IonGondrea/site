// Fetch countries from the API
async function loadCountries() {
  try {
    const response = await fetch('/api/countries');
    if (!response.ok) {
      throw new Error('Failed to load countries');
    }
    const countries = await response.json();
    displayCountries(countries);
  } catch (error) {
    console.error('Error loading countries:', error);
    const grid = document.getElementById('countries-grid');
    grid.innerHTML = '<div class="error">Failed to load countries. Please refresh the page.</div>';
  }
}

// Display countries in the grid
function displayCountries(countries) {
  const grid = document.getElementById('countries-grid');
  grid.innerHTML = '';

  countries.forEach(country => {
    const card = createCountryCard(country);
    grid.appendChild(card);
  });
}

// Create a country card element
function createCountryCard(country) {
  const card = document.createElement('div');
  card.className = 'country-card';
  card.setAttribute('data-country', country.id);
  
  // Handle click to redirect
  card.addEventListener('click', () => {
    window.location.href = country.redirectUrl;
  });

  // Create flag image
  const flagImg = document.createElement('img');
  flagImg.src = country.flag;
  flagImg.alt = `${country.name} flag`;
  flagImg.className = 'country-flag';
  
  // Handle image load error
  flagImg.onerror = () => {
    flagImg.src = 'images/placeholder.svg';
  };

  // Create name label
  const nameLabel = document.createElement('div');
  nameLabel.className = 'country-name';
  nameLabel.textContent = country.name;

  card.appendChild(flagImg);
  card.appendChild(nameLabel);

  return card;
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
  loadCountries();
});
