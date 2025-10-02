import { AxiosRequestConfig } from 'axios';

// Create a variable to export the mock instance
// Using var for function-level hoisting to avoid initialization errors
var mockAxiosInstance: any;

// Mock axios BEFORE importing the api module
// The factory function is hoisted, so we create the mock instance inside it
jest.mock('axios', () => {
  const instance = {
    get: jest.fn(),
    post: jest.fn(),
    put: jest.fn(),
    delete: jest.fn(),
    interceptors: {
      request: {
        use: jest.fn((onFulfilled, onRejected) => 0),
        eject: jest.fn(),
        clear: jest.fn(),
      },
      response: {
        use: jest.fn((onFulfilled, onRejected) => 0),
        eject: jest.fn(),
        clear: jest.fn(),
      },
    },
    defaults: {
      headers: {
        common: {},
      },
    },
  };

  // Assign to outer scope
  mockAxiosInstance = instance;

  return {
    create: jest.fn(() => instance),
    default: {
      create: jest.fn(() => instance),
    },
  };
});

// NOW import the api module - it will use our mocked axios.create
import axios from 'axios';
import { apiClient } from '../api';

describe('ApiClient', () => {
  beforeEach(() => {
    // Clear all mocks before each test (but after the axios.create call during import)
    // Only clear the method mocks, not the axios.create mock
    mockAxiosInstance.get.mockClear();
    mockAxiosInstance.post.mockClear();
    mockAxiosInstance.put.mockClear();
    mockAxiosInstance.delete.mockClear();
  });

  describe('Initialization', () => {
    it('should have apiClient instance available', () => {
      // Verify that apiClient was successfully instantiated
      expect(apiClient).toBeDefined();
      expect(apiClient.get).toBeDefined();
      expect(apiClient.post).toBeDefined();
      expect(apiClient.put).toBeDefined();
      expect(apiClient.delete).toBeDefined();
      expect(apiClient.getBlob).toBeDefined();
      expect(apiClient.fetchCSRFToken).toBeDefined();
    });

    it('should have created axios instance', () => {
      // Verify that the mock axios instance was created with interceptors
      expect(mockAxiosInstance).toBeDefined();
      expect(mockAxiosInstance.interceptors).toBeDefined();
      expect(mockAxiosInstance.interceptors.request).toBeDefined();
      expect(mockAxiosInstance.interceptors.response).toBeDefined();
    });

    it('should have HTTP methods available on axios instance', () => {
      // Verify that all HTTP methods are available
      expect(mockAxiosInstance.get).toBeDefined();
      expect(mockAxiosInstance.post).toBeDefined();
      expect(mockAxiosInstance.put).toBeDefined();
      expect(mockAxiosInstance.delete).toBeDefined();
    });
  });

  describe('GET requests', () => {
    it('should make GET request and return data', async () => {
      const mockData = { id: 1, name: 'test' };
      mockAxiosInstance.get.mockResolvedValue({ data: mockData });

      const result = await apiClient.get('/test-endpoint');

      expect(mockAxiosInstance.get).toHaveBeenCalledWith('/test-endpoint', undefined);
      expect(result).toEqual(mockData);
    });

    it('should pass config options to GET request', async () => {
      const mockData = { data: 'test' };
      const config: AxiosRequestConfig = { params: { page: 1 } };
      mockAxiosInstance.get.mockResolvedValue({ data: mockData });

      await apiClient.get('/test-endpoint', config);

      expect(mockAxiosInstance.get).toHaveBeenCalledWith('/test-endpoint', config);
    });

    it('should handle GET request with query parameters', async () => {
      const mockData = { results: [] };
      const config: AxiosRequestConfig = { params: { search: 'test', limit: 10 } };
      mockAxiosInstance.get.mockResolvedValue({ data: mockData });

      const result = await apiClient.get('/search', config);

      expect(mockAxiosInstance.get).toHaveBeenCalledWith('/search', config);
      expect(result).toEqual(mockData);
    });

    it('should handle GET request errors', async () => {
      const error = new Error('Network error');
      mockAxiosInstance.get.mockRejectedValue(error);

      await expect(apiClient.get('/test-endpoint')).rejects.toThrow('Network error');
    });

    it('should handle GET request with custom headers', async () => {
      const mockData = { data: 'test' };
      const config: AxiosRequestConfig = {
        headers: { 'Authorization': 'Bearer token' }
      };
      mockAxiosInstance.get.mockResolvedValue({ data: mockData });

      await apiClient.get('/protected', config);

      expect(mockAxiosInstance.get).toHaveBeenCalledWith('/protected', config);
    });
  });

  describe('POST requests', () => {
    it('should make POST request and return data', async () => {
      const mockData = { success: true };
      const postData = { name: 'test' };
      mockAxiosInstance.post.mockResolvedValue({ data: mockData });

      const result = await apiClient.post('/test-endpoint', postData);

      expect(mockAxiosInstance.post).toHaveBeenCalledWith('/test-endpoint', postData, undefined);
      expect(result).toEqual(mockData);
    });

    it('should make POST request without data', async () => {
      const mockData = { success: true };
      mockAxiosInstance.post.mockResolvedValue({ data: mockData });

      const result = await apiClient.post('/test-endpoint');

      expect(mockAxiosInstance.post).toHaveBeenCalledWith('/test-endpoint', undefined, undefined);
      expect(result).toEqual(mockData);
    });

    it('should pass config options to POST request', async () => {
      const mockData = { success: true };
      const postData = { name: 'test' };
      const config: AxiosRequestConfig = { headers: { 'Custom-Header': 'value' } };
      mockAxiosInstance.post.mockResolvedValue({ data: mockData });

      await apiClient.post('/test-endpoint', postData, config);

      expect(mockAxiosInstance.post).toHaveBeenCalledWith('/test-endpoint', postData, config);
    });

    it('should handle POST request with complex data', async () => {
      const mockData = { id: 123, created: true };
      const postData = {
        user: { name: 'John', email: 'john@example.com' },
        settings: { theme: 'dark', notifications: true }
      };
      mockAxiosInstance.post.mockResolvedValue({ data: mockData });

      const result = await apiClient.post('/users', postData);

      expect(mockAxiosInstance.post).toHaveBeenCalledWith('/users', postData, undefined);
      expect(result).toEqual(mockData);
    });

    it('should handle POST request errors', async () => {
      const error = new Error('Server error');
      mockAxiosInstance.post.mockRejectedValue(error);

      await expect(apiClient.post('/test-endpoint', {})).rejects.toThrow('Server error');
    });

    it('should handle POST request with validation errors', async () => {
      const error = {
        response: {
          status: 400,
          data: { errors: ['Invalid input'] }
        }
      };
      mockAxiosInstance.post.mockRejectedValue(error);

      await expect(apiClient.post('/validate', {})).rejects.toEqual(error);
    });
  });

  describe('PUT requests', () => {
    it('should make PUT request and return data', async () => {
      const mockData = { updated: true };
      const putData = { name: 'updated' };
      mockAxiosInstance.put.mockResolvedValue({ data: mockData });

      const result = await apiClient.put('/test-endpoint', putData);

      expect(mockAxiosInstance.put).toHaveBeenCalledWith('/test-endpoint', putData, undefined);
      expect(result).toEqual(mockData);
    });

    it('should make PUT request without data', async () => {
      const mockData = { updated: true };
      mockAxiosInstance.put.mockResolvedValue({ data: mockData });

      const result = await apiClient.put('/test-endpoint');

      expect(mockAxiosInstance.put).toHaveBeenCalledWith('/test-endpoint', undefined, undefined);
      expect(result).toEqual(mockData);
    });

    it('should pass config options to PUT request', async () => {
      const mockData = { updated: true };
      const putData = { name: 'updated' };
      const config: AxiosRequestConfig = { headers: { 'Custom-Header': 'value' } };
      mockAxiosInstance.put.mockResolvedValue({ data: mockData });

      await apiClient.put('/test-endpoint', putData, config);

      expect(mockAxiosInstance.put).toHaveBeenCalledWith('/test-endpoint', putData, config);
    });

    it('should handle PUT request errors', async () => {
      const error = new Error('Update error');
      mockAxiosInstance.put.mockRejectedValue(error);

      await expect(apiClient.put('/test-endpoint', {})).rejects.toThrow('Update error');
    });

    it('should handle PUT request with partial updates', async () => {
      const mockData = { id: 1, name: 'updated', email: 'old@example.com' };
      const putData = { name: 'updated' };
      mockAxiosInstance.put.mockResolvedValue({ data: mockData });

      const result = await apiClient.put('/users/1', putData);

      expect(result).toEqual(mockData);
    });
  });

  describe('DELETE requests', () => {
    it('should make DELETE request and return data', async () => {
      const mockData = { deleted: true };
      mockAxiosInstance.delete.mockResolvedValue({ data: mockData });

      const result = await apiClient.delete('/test-endpoint');

      expect(mockAxiosInstance.delete).toHaveBeenCalledWith('/test-endpoint', undefined);
      expect(result).toEqual(mockData);
    });

    it('should pass config options to DELETE request', async () => {
      const mockData = { deleted: true };
      const config: AxiosRequestConfig = { headers: { 'Custom-Header': 'value' } };
      mockAxiosInstance.delete.mockResolvedValue({ data: mockData });

      await apiClient.delete('/test-endpoint', config);

      expect(mockAxiosInstance.delete).toHaveBeenCalledWith('/test-endpoint', config);
    });

    it('should handle DELETE request with response data', async () => {
      const mockData = { id: 123, message: 'Resource deleted successfully' };
      mockAxiosInstance.delete.mockResolvedValue({ data: mockData });

      const result = await apiClient.delete('/resources/123');

      expect(result).toEqual(mockData);
    });

    it('should handle DELETE request errors', async () => {
      const error = new Error('Delete error');
      mockAxiosInstance.delete.mockRejectedValue(error);

      await expect(apiClient.delete('/test-endpoint')).rejects.toThrow('Delete error');
    });

    it('should handle DELETE request with 404 error', async () => {
      const error = {
        response: {
          status: 404,
          data: { message: 'Resource not found' }
        }
      };
      mockAxiosInstance.delete.mockRejectedValue(error);

      await expect(apiClient.delete('/resources/999')).rejects.toEqual(error);
    });
  });

  describe('getBlob', () => {
    it('should make GET request with blob responseType', async () => {
      const mockBlob = new Blob(['test'], { type: 'application/pdf' });
      mockAxiosInstance.get.mockResolvedValue({ data: mockBlob });

      const result = await apiClient.getBlob('/test-file');

      expect(mockAxiosInstance.get).toHaveBeenCalledWith(
        '/test-file',
        expect.objectContaining({ responseType: 'blob' })
      );
      expect(result).toEqual(mockBlob);
    });

    it('should merge config with responseType', async () => {
      const mockBlob = new Blob(['test'], { type: 'text/plain' });
      const config: AxiosRequestConfig = { headers: { 'Custom-Header': 'value' } };
      mockAxiosInstance.get.mockResolvedValue({ data: mockBlob });

      await apiClient.getBlob('/test-file', config);

      expect(mockAxiosInstance.get).toHaveBeenCalledWith(
        '/test-file',
        expect.objectContaining({
          responseType: 'blob',
          headers: { 'Custom-Header': 'value' },
        })
      );
    });

    it('should handle different blob types', async () => {
      const mockBlob = new Blob(['image data'], { type: 'image/png' });
      mockAxiosInstance.get.mockResolvedValue({ data: mockBlob });

      const result = await apiClient.getBlob('/images/logo.png');

      expect(result).toEqual(mockBlob);
      expect(result.type).toBe('image/png');
    });

    it('should handle getBlob errors', async () => {
      const error = new Error('Blob download failed');
      mockAxiosInstance.get.mockRejectedValue(error);

      await expect(apiClient.getBlob('/missing-file')).rejects.toThrow('Blob download failed');
    });
  });

  describe('fetchCSRFToken', () => {
    it('should fetch CSRF token successfully', async () => {
      const mockToken = 'test-csrf-token-12345';
      mockAxiosInstance.get.mockResolvedValue({ data: { token: mockToken } });

      await apiClient.fetchCSRFToken();

      expect(mockAxiosInstance.get).toHaveBeenCalledWith('/api/csrf-token');
    });

    it('should handle CSRF token fetch error', async () => {
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
      const error = new Error('CSRF token error');
      mockAxiosInstance.get.mockRejectedValue(error);

      await apiClient.fetchCSRFToken();

      expect(consoleSpy).toHaveBeenCalledWith('Failed to fetch CSRF token:', error);
      consoleSpy.mockRestore();
    });

    it('should handle CSRF token with empty response', async () => {
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
      mockAxiosInstance.get.mockResolvedValue({ data: {} });

      await apiClient.fetchCSRFToken();

      // Should not throw error even if token is undefined
      expect(mockAxiosInstance.get).toHaveBeenCalledWith('/api/csrf-token');
      consoleSpy.mockRestore();
    });
  });

  describe('Type safety', () => {
    it('should return typed data for GET requests', async () => {
      interface User {
        id: number;
        name: string;
      }
      const mockUser: User = { id: 1, name: 'John' };
      mockAxiosInstance.get.mockResolvedValue({ data: mockUser });

      const result = await apiClient.get<User>('/users/1');

      expect(result).toEqual(mockUser);
      expect(result.id).toBe(1);
      expect(result.name).toBe('John');
    });

    it('should return typed data for POST requests', async () => {
      interface CreateResponse {
        id: number;
        created: boolean;
      }
      const mockResponse: CreateResponse = { id: 1, created: true };
      mockAxiosInstance.post.mockResolvedValue({ data: mockResponse });

      const result = await apiClient.post<CreateResponse>('/users', { name: 'John' });

      expect(result).toEqual(mockResponse);
      expect(result.created).toBe(true);
    });

    it('should return typed data for PUT requests', async () => {
      interface UpdateResponse {
        id: number;
        updated: boolean;
        timestamp: string;
      }
      const mockResponse: UpdateResponse = {
        id: 1,
        updated: true,
        timestamp: '2025-10-02T00:00:00Z'
      };
      mockAxiosInstance.put.mockResolvedValue({ data: mockResponse });

      const result = await apiClient.put<UpdateResponse>('/users/1', { name: 'Jane' });

      expect(result).toEqual(mockResponse);
      expect(result.updated).toBe(true);
    });

    it('should return typed data for DELETE requests', async () => {
      interface DeleteResponse {
        id: number;
        deleted: boolean;
      }
      const mockResponse: DeleteResponse = { id: 1, deleted: true };
      mockAxiosInstance.delete.mockResolvedValue({ data: mockResponse });

      const result = await apiClient.delete<DeleteResponse>('/users/1');

      expect(result).toEqual(mockResponse);
      expect(result.deleted).toBe(true);
    });
  });

  describe('Error handling', () => {
    it('should handle response errors with status code', async () => {
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
      const error = {
        response: {
          status: 404,
          data: { message: 'Not found' },
        },
      };
      mockAxiosInstance.get.mockRejectedValue(error);

      await expect(apiClient.get('/not-found')).rejects.toEqual(error);

      consoleSpy.mockRestore();
    });

    it('should handle network errors', async () => {
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
      const error = {
        request: {},
        message: 'Network Error',
      };
      mockAxiosInstance.get.mockRejectedValue(error);

      await expect(apiClient.get('/test')).rejects.toEqual(error);

      consoleSpy.mockRestore();
    });

    it('should handle 500 server errors', async () => {
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
      const error = {
        response: {
          status: 500,
          data: { message: 'Internal server error' },
        },
      };
      mockAxiosInstance.post.mockRejectedValue(error);

      await expect(apiClient.post('/error-endpoint', {})).rejects.toEqual(error);

      consoleSpy.mockRestore();
    });

    it('should handle unauthorized errors', async () => {
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
      const error = {
        response: {
          status: 401,
          data: { message: 'Unauthorized' },
        },
      };
      mockAxiosInstance.get.mockRejectedValue(error);

      await expect(apiClient.get('/protected')).rejects.toEqual(error);

      consoleSpy.mockRestore();
    });

    it('should handle timeout errors', async () => {
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
      const error = {
        code: 'ECONNABORTED',
        message: 'timeout of 10000ms exceeded',
      };
      mockAxiosInstance.get.mockRejectedValue(error);

      await expect(apiClient.get('/slow-endpoint')).rejects.toEqual(error);

      consoleSpy.mockRestore();
    });
  });

  describe('Integration scenarios', () => {
    it('should handle multiple sequential requests', async () => {
      const mockData1 = { id: 1 };
      const mockData2 = { id: 2 };

      mockAxiosInstance.get.mockResolvedValueOnce({ data: mockData1 });
      mockAxiosInstance.post.mockResolvedValueOnce({ data: mockData2 });

      const result1 = await apiClient.get('/endpoint1');
      const result2 = await apiClient.post('/endpoint2', {});

      expect(result1).toEqual(mockData1);
      expect(result2).toEqual(mockData2);
      expect(mockAxiosInstance.get).toHaveBeenCalledTimes(1);
      expect(mockAxiosInstance.post).toHaveBeenCalledTimes(1);
    });

    it('should handle concurrent requests', async () => {
      const mockData1 = { id: 1 };
      const mockData2 = { id: 2 };

      mockAxiosInstance.get.mockResolvedValueOnce({ data: mockData1 });
      mockAxiosInstance.get.mockResolvedValueOnce({ data: mockData2 });

      const [result1, result2] = await Promise.all([
        apiClient.get('/endpoint1'),
        apiClient.get('/endpoint2'),
      ]);

      expect(result1).toEqual(mockData1);
      expect(result2).toEqual(mockData2);
      expect(mockAxiosInstance.get).toHaveBeenCalledTimes(2);
    });

    it('should handle mixed success and error responses', async () => {
      const mockData = { id: 1 };
      const error = new Error('Request failed');

      mockAxiosInstance.get.mockResolvedValueOnce({ data: mockData });
      mockAxiosInstance.get.mockRejectedValueOnce(error);

      const result1 = await apiClient.get('/success');

      await expect(apiClient.get('/error')).rejects.toThrow('Request failed');

      expect(result1).toEqual(mockData);
    });

    it('should handle request retries', async () => {
      const mockData = { id: 1, success: true };

      mockAxiosInstance.get
        .mockRejectedValueOnce(new Error('First attempt failed'))
        .mockResolvedValueOnce({ data: mockData });

      // Simulate retry logic
      let result;
      try {
        result = await apiClient.get('/unstable-endpoint');
      } catch {
        result = await apiClient.get('/unstable-endpoint');
      }

      expect(result).toEqual(mockData);
      expect(mockAxiosInstance.get).toHaveBeenCalledTimes(2);
    });

    it('should handle empty response data', async () => {
      mockAxiosInstance.get.mockResolvedValue({ data: null });

      const result = await apiClient.get('/empty');

      expect(result).toBeNull();
    });

    it('should handle array response data', async () => {
      const mockData = [
        { id: 1, name: 'Item 1' },
        { id: 2, name: 'Item 2' },
      ];
      mockAxiosInstance.get.mockResolvedValue({ data: mockData });

      const result = await apiClient.get('/items');

      expect(result).toEqual(mockData);
      expect(Array.isArray(result)).toBe(true);
      expect(result).toHaveLength(2);
    });
  });
});
