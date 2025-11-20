import { APIRequestContext, APIResponse } from '@playwright/test';
import { ApiHelper } from './api-helper';

export class UserApiClient extends ApiHelper {
  private readonly endpoints = {
    users: '/users',
    user: (id: string) => `/users/${id}`,
    login: '/auth/login',
    logout: '/auth/logout'
  };

  constructor(context: APIRequestContext) {
    super(context);
  }

  /**
   * Get all users
   */
  async getAllUsers(): Promise<APIResponse> {
    return await this.get(this.endpoints.users);
  }

  /**
   * Get user by ID
   */
  async getUserById(userId: string): Promise<APIResponse> {
    return await this.get(this.endpoints.user(userId));
  }

  /**
   * Create a new user
   */
  async createUser(userData: any): Promise<APIResponse> {
    return await this.post(this.endpoints.users, userData);
  }

  /**
   * Update user
   */
  async updateUser(userId: string, userData: any): Promise<APIResponse> {
    return await this.put(this.endpoints.user(userId), userData);
  }

  /**
   * Delete user
   */
  async deleteUser(userId: string): Promise<APIResponse> {
    return await this.delete(this.endpoints.user(userId));
  }

  /**
   * Login user
   */
  async login(email: string, password: string): Promise<APIResponse> {
    return await this.post(this.endpoints.login, { email, password });
  }

  /**
   * Logout user
   */
  async logout(): Promise<APIResponse> {
    return await this.post(this.endpoints.logout);
  }
}
