import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ProductListPage } from './ProductListPage';

vi.mock('../../../services/productsApi', () => ({
  getProducts: () => Promise.resolve([
    { id: '1', brand: 'Apple', model: 'iPhone 13', price: '799', imgUrl: '/iphone.png' },
    { id: '2', brand: 'Samsung', model: 'Galaxy S22', price: '699', imgUrl: '/galaxy.png' }
  ])
}));

describe('ProductListPage', () => {
  beforeEach(() => window.localStorage.clear());

  it('filters by brand or model in real time', async () => {
    render(<MemoryRouter><ProductListPage /></MemoryRouter>);

    await waitFor(() => expect(screen.getByText('iPhone 13')).toBeInTheDocument());

    await userEvent.type(screen.getByLabelText(/buscar por marca o modelo/i), 'Samsung');

    expect(screen.queryByText('iPhone 13')).not.toBeInTheDocument();
    expect(screen.getByText('Galaxy S22')).toBeInTheDocument();
  });
});
