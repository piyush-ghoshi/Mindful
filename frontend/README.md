# Mindful Wellness Platform - Frontend

A comprehensive React TypeScript frontend for the Mindful Wellness Platform, a student mental health and wellness support system.

## Project Overview

This is the client-side application for the Mindful Wellness Platform, built with:

- **React 19** - UI library
- **TypeScript** - Type-safe JavaScript
- **Vite** - Fast build tool and dev server
- **ESLint** - Code quality and linting
- **Prettier** - Code formatting

## Features

- 📝 **Mood Tracking** - Daily mood check-ins and journal entries
- 👥 **Appointment Booking** - Schedule counseling sessions
- 💬 **AI Chatbot** - Immediate support and guidance
- 📚 **Resource Hub** - Curated wellness materials
- 🎮 **Gamification** - Points, badges, and leaderboards
- 🆘 **Crisis Support** - Emergency resources and hotlines
- 👥 **Peer Forum** - Community support and discussions
- 📊 **Analytics** - Admin dashboards and reports
- 🔐 **Secure Messaging** - End-to-end encrypted chat
- 🌍 **Multi-Language Support** - Internationalization ready

## Project Structure

```
frontend/
├── src/
│   ├── components/          # Reusable React components
│   ├── pages/              # Page components
│   ├── services/           # API and business logic services
│   ├── types/              # TypeScript type definitions
│   ├── hooks/              # Custom React hooks
│   ├── utils/              # Utility functions
│   ├── constants/          # Application constants
│   ├── context/            # React context providers
│   ├── styles/             # Global and component styles
│   ├── App.tsx             # Root component
│   ├── main.tsx            # Entry point
│   └── index.css           # Global styles
├── public/                 # Static assets
├── .env.example            # Environment variables template
├── .env.local              # Local environment variables
├── .prettierrc.json        # Prettier configuration
├── .prettierignore         # Prettier ignore rules
├── eslint.config.js        # ESLint configuration
├── vite.config.ts          # Vite configuration
├── tsconfig.json           # TypeScript configuration
└── package.json            # Dependencies and scripts
```

## Getting Started

### Prerequisites

- Node.js 16+ and npm 7+
- Backend API running on `http://localhost:8080`

### Installation

1. Install dependencies:

```bash
npm install
```

2. Create a `.env.local` file based on `.env.example`:

```bash
cp .env.example .env.local
```

3. Update `.env.local` with your API configuration:

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_WEBSOCKET_URL=ws://localhost:8080/ws
```

### Development

Start the development server:

```bash
npm run dev
```

The application will be available at `http://localhost:5173`

### Building

Build for production:

```bash
npm run build
```

Preview the production build:

```bash
npm run preview
```

## Available Scripts

- `npm run dev` - Start development server with hot reload
- `npm run build` - Build for production
- `npm run lint` - Run ESLint to check code quality
- `npm run lint:fix` - Fix ESLint issues automatically
- `npm run format` - Format code with Prettier
- `npm run format:check` - Check code formatting
- `npm run preview` - Preview production build locally

## Code Quality

### ESLint

ESLint is configured to enforce code quality standards. Run linting:

```bash
npm run lint
npm run lint:fix  # Auto-fix issues
```

### Prettier

Prettier is configured for consistent code formatting. Format your code:

```bash
npm run format
npm run format:check  # Check without modifying
```

## Environment Variables

Create a `.env.local` file in the project root. See `.env.example` for all available variables:

```env
# API Configuration
VITE_API_BASE_URL=http://localhost:8080/api
VITE_API_TIMEOUT=30000

# Authentication
VITE_AUTH_TOKEN_KEY=mindful_auth_token
VITE_AUTH_REFRESH_TOKEN_KEY=mindful_refresh_token

# WebSocket
VITE_WEBSOCKET_URL=ws://localhost:8080/ws

# Feature Flags
VITE_ENABLE_CHATBOT=true
VITE_ENABLE_FORUM=true
VITE_ENABLE_ANALYTICS=true

# Environment
VITE_ENVIRONMENT=development
VITE_LOG_LEVEL=debug

# Multi-language
VITE_DEFAULT_LANGUAGE=en
VITE_SUPPORTED_LANGUAGES=en,es,fr,de
```

## API Integration

The frontend communicates with the backend API through the `ApiClient` service:

```typescript
import { apiClient } from './services/api';

// GET request
const data = await apiClient.get('/endpoint');

// POST request
const result = await apiClient.post('/endpoint', { data });

// PUT request
const updated = await apiClient.put('/endpoint', { data });

// DELETE request
await apiClient.delete('/endpoint');

// File upload
const uploaded = await apiClient.uploadFile('/upload', file);
```

## Authentication

Authentication is handled by the `AuthService`:

```typescript
import { authService } from './services/authService';

// Login
const response = await authService.login({ email, password });

// Register
const response = await authService.register({ email, password, firstName, lastName, role });

// Logout
await authService.logout();

// Check authentication
const isAuthenticated = authService.isAuthenticated();

// Get current user
const user = await authService.getCurrentUser();
```

## Type Definitions

All TypeScript types are defined in `src/types/index.ts`:

- `User`, `StudentProfile`, `CounsellorProfile`
- `Appointment`, `MoodEntry`, `Message`
- `ForumPost`, `Resource`, `WellnessGoal`
- `Notification`, `ApiResponse`, `ApiError`

## Styling

The project uses CSS with CSS variables for theming:

```css
:root {
  --primary-color: #6366f1;
  --secondary-color: #8b5cf6;
  --success-color: #10b981;
  --danger-color: #ef4444;
  /* ... more variables */
}
```

Global styles are in `src/index.css`. Component-specific styles should be co-located with components.

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## Performance

- Code splitting with Vite
- Lazy loading for routes
- Image optimization
- Bundle analysis available

## Accessibility

The project aims for WCAG 2.1 Level AA compliance:

- Semantic HTML
- ARIA labels and roles
- Keyboard navigation
- Color contrast compliance
- Screen reader support

## Contributing

1. Create a feature branch: `git checkout -b feature/your-feature`
2. Make your changes and commit: `git commit -am 'Add feature'`
3. Push to the branch: `git push origin feature/your-feature`
4. Submit a pull request

## Code Style

- Follow ESLint rules
- Format with Prettier before committing
- Use TypeScript for type safety
- Write meaningful commit messages

## Troubleshooting

### Port Already in Use

If port 5173 is already in use, Vite will automatically use the next available port.

### API Connection Issues

1. Ensure the backend is running on the configured URL
2. Check CORS configuration on the backend
3. Verify environment variables in `.env.local`

### Build Errors

1. Clear `node_modules` and reinstall: `rm -rf node_modules && npm install`
2. Clear Vite cache: `rm -rf dist`
3. Check TypeScript errors: `npm run build`

## License

This project is part of the Mindful Wellness Platform. All rights reserved.

## Support

For issues or questions, please contact the development team or create an issue in the project repository.
