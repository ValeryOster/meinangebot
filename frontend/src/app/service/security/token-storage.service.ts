import { Injectable } from '@angular/core';

const TOKEN_KEY = 'auth-token';
const USER_KEY = 'auth-user';
const USER_ROLES = 'auth-roles';

@Injectable({
  providedIn: 'root'
})
export class TokenStorageService {

  constructor() { }

  signOut() {
    window.sessionStorage.clear();
  }

  public saveRoles(roles: []) {
    window.sessionStorage.removeItem(USER_ROLES);
    window.sessionStorage.setItem(USER_ROLES, JSON.stringify(roles));
  }

  public getRoles(): string[] {
    return JSON.parse(window.sessionStorage.getItem(USER_ROLES));
  }

  public saveToken(token: string) {
    window.sessionStorage.removeItem(TOKEN_KEY);
    window.sessionStorage.setItem(TOKEN_KEY, token);
  }

  public getToken(): string {
    return sessionStorage.getItem(TOKEN_KEY);
  }

  public saveUser(user) {
    window.sessionStorage.removeItem(USER_KEY);
    window.sessionStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  public getUser() {
    return JSON.parse(sessionStorage.getItem(USER_KEY));
  }

  public isAuthenticated():boolean{
    return !!sessionStorage.getItem(TOKEN_KEY)
  }
}
