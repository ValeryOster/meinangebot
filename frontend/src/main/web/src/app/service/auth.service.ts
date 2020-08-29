import {Injectable} from '@angular/core';

@Injectable({providedIn: 'root'})
export class AuthService {
  private isAuth = false;
  private url = 'http://localhost:8080/';

  login() {
    this.isAuth = true;
  }

  logout() {
    this.isAuth = false;
  }

  isAuthenticated(): Promise<boolean> {
    return new Promise<boolean>(resolve => {
      setTimeout(() => {
        resolve(this.isAuth);
      }, 1000);
    });
  }
}
