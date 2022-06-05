import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {HomeComponent} from "./home/home.component";
import {ManageComponent} from "./manage/manage.component";
import {AuthGuard} from "./service/security/auth.guard";
import {LoginComponent} from "./login/login.component";
import {AuswahlComponent} from "./auswahl/auswahl.component";


const routes: Routes = [
  {path: '', component: HomeComponent},
  {path: 'auswahl', component: AuswahlComponent},
  {
    path: 'manage', component: ManageComponent, canActivate: [AuthGuard], data: {
      role: 'ROLE_ADMIN'
    }
  },
  {path: 'login', component: LoginComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
