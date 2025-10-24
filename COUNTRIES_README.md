# Country Redirect Website

A Java web application that displays a grid of countries with flags and redirects users to country-specific URLs when they click on a country.

## Features

- **Country Selection Interface**: Modern, responsive grid layout displaying countries with their flags
- **Click-to-Redirect**: Clicking any country automatically redirects to its configured URL
- **Easy Configuration**: Simple JSON file for managing countries and their redirect URLs
- **Customizable Images**: Support for custom flag/country images
- **Responsive Design**: Works on desktop, tablet, and mobile devices

## Quick Start

### Build the Project

```bash
mvn clean package
```

### Run the Server

```bash
mvn -q exec:java
```

The server will start on `http://localhost:4567` and automatically open in your browser.

## Configuration

### Adding or Modifying Countries

Edit the `src/public/countries.json` file to configure countries:

```json
[
  {
    "id": "usa",
    "name": "United States",
    "flag": "images/flags/usa.png",
    "redirectUrl": "https://example.com/us"
  },
  {
    "id": "uk",
    "name": "United Kingdom",
    "flag": "images/flags/uk.png",
    "redirectUrl": "https://example.com/uk"
  }
]
```

**Fields:**
- `id`: Unique identifier for the country (lowercase, no spaces)
- `name`: Display name shown under the flag
- `flag`: Path to the flag image (relative to `src/public/`)
- `redirectUrl`: URL to redirect to when the country is clicked

### Adding Custom Flag Images

1. Place your flag images in `src/public/images/flags/`
2. Recommended format: PNG or SVG
3. Recommended size: 200x120 pixels (5:3 aspect ratio)
4. Update the `flag` path in `countries.json` to point to your image

Example:
```json
{
  "id": "mexico",
  "name": "Mexico",
  "flag": "[images/flags/mexico.png](https://th.bing.com/th/id/R.0737629c397c86c1bb392066a06ba31a?rik=KGu3D5DE%2fWhhjw&riu=http%3a%2f%2fupload.wikimedia.org%2fwikipedia%2fcommons%2f1%2f17%2fFlag_of_Mexico.png&ehk=MAXespfiLeO3ZBPldQ3%2fNDkCjzGo7hljFBd3yKgqLTY%3d&risl=&pid=ImgRaw&r=0)",
  "redirectUrl": "https://example.com/mx"
}
```

### Using Remote Images

You can also use remote image URLs:

```json
{
  "id": "mexico",
  "name": "Mexico",
  "flag": "https://example.com/images/mexico-flag.png",
  "redirectUrl": "https://example.com/mx"
}
```

## Customization

### Styling

Edit `src/public/styles.css` to customize:
- Colors and gradients
- Card sizes and spacing
- Hover effects and animations
- Typography

### Page Title and Header

Edit `src/public/index.html` to customize:
- Page title: `<title>Your Title</title>`
- Header text: `<h1>Your Header</h1>`
- Subtitle: `<p class="subtitle">Your subtitle</p>`

### Server Configuration

Edit `src/Server.java` to configure:
- Port number (default: 4567)
- Static files location
- API endpoints

## Project Structure

```
site/
├── src/
│   ├── public/              # Web frontend files
│   │   ├── index.html       # Main page
│   │   ├── styles.css       # Styles
│   │   ├── countries.js     # JavaScript logic
│   │   ├── countries.json   # Country configuration
│   │   └── images/
│   │       └── flags/       # Flag images
│   ├── Server.java          # Spark Java web server
│   └── [other Java files]
├── pom.xml                  # Maven configuration
└── README.md
```

## API Endpoints

- `GET /api/countries` - Returns the list of countries from `countries.json`
- `GET /health` - Health check endpoint

## Technology Stack

- **Backend**: Java 11 with Spark Java framework
- **Frontend**: Vanilla JavaScript, HTML5, CSS3
- **Build Tool**: Maven
- **Database**: H2 (for other features)

## Tips

1. **Testing Changes**: After modifying `countries.json` or images, simply refresh your browser - no rebuild needed!

2. **Placeholder Images**: If a flag image fails to load, a placeholder will be shown automatically.

3. **Multiple Environments**: Use different `countries.json` files for different environments (dev, staging, production).

4. **Large Country Lists**: The grid automatically adjusts to display any number of countries in a responsive layout.

## Troubleshooting

**Issue**: Countries not loading
- Check that `src/public/countries.json` exists and has valid JSON
- Verify the console in browser DevTools for errors

**Issue**: Flag images not displaying
- Ensure images are in `src/public/images/flags/`
- Check file names match exactly (case-sensitive)
- Verify image formats are supported (PNG, SVG, JPG)

**Issue**: Redirect not working
- Verify `redirectUrl` is a valid, complete URL (including `https://`)
- Check browser console for JavaScript errors

## Example Countries Configuration

Here's a template for common countries:

```json
[
  {"id": "usa", "name": "United States", "flag": "images/flags/usa.png", "redirectUrl": "https://example.com/us"},
  {"id": "uk", "name": "United Kingdom", "flag": "images/flags/uk.png", "redirectUrl": "https://example.com/uk"},
  {"id": "canada", "name": "Canada", "flag": "images/flags/canada.png", "redirectUrl": "https://example.com/ca"},
  {"id": "germany", "name": "Germany", "flag": "images/flags/germany.png", "redirectUrl": "https://example.com/de"},
  {"id": "france", "name": "France", "flag": "images/flags/france.png", "redirectUrl": "https://example.com/fr"},
  {"id": "spain", "name": "Spain", "flag": "images/flags/spain.png", "redirectUrl": "https://example.com/es"},
  {"id": "italy", "name": "Italy", "flag": "images/flags/italy.png", "redirectUrl": "https://example.com/it"},
  {"id": "japan", "name": "Japan", "flag": "images/flags/japan.png", "redirectUrl": "https://example.com/jp"},
  {"id": "australia", "name": "Australia", "flag": "images/flags/australia.png", "redirectUrl": "https://example.com/au"},
  {"id": "brazil", "name": "Brazil", "flag": "images/flags/brazil.png", "redirectUrl": "https://example.com/br"}
]
```

## License

This project is provided as-is for customization and use.
