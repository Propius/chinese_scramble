import axios, { AxiosInstance, AxiosRequestConfig, AxiosError } from 'axios';
import { API_BASE_URL } from '../constants/game.constants';

class ApiClient {
  private instance: AxiosInstance;
  private csrfToken: string = '';

  constructor(baseURL: string = API_BASE_URL) {
    this.instance = axios.create({
      baseURL,
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json',
      },
      withCredentials: true,
    });

    this.setupInterceptors();
  }

  private setupInterceptors(): void {
    this.instance.interceptors.request.use(
      (config) => {
        if (this.csrfToken && config.method !== 'get') {
          config.headers['X-CSRF-Token'] = this.csrfToken;
        }
        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );

    this.instance.interceptors.response.use(
      (response) => response,
      (error: AxiosError) => {
        if (error.response) {
          console.error('API Error:', error.response.status, error.response.data);
        } else if (error.request) {
          console.error('Network Error:', error.message);
        }
        return Promise.reject(error);
      }
    );
  }

  public async fetchCSRFToken(): Promise<void> {
    try {
      const response = await this.instance.get('/api/csrf-token');
      this.csrfToken = response.data.token;
    } catch (error) {
      console.error('Failed to fetch CSRF token:', error);
    }
  }

  public get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.get<T>(url, config).then(response => response.data);
  }

  public post<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.post<T>(url, data, config).then(response => response.data);
  }

  public put<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.put<T>(url, data, config).then(response => response.data);
  }

  public delete<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.delete<T>(url, config).then(response => response.data);
  }

  public getBlob(url: string, config?: AxiosRequestConfig): Promise<Blob> {
    return this.instance.get<Blob>(url, { ...config, responseType: 'blob' }).then(response => response.data);
  }
}

export const apiClient = new ApiClient();
export default apiClient;