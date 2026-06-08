import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {HomeComponent} from "./home/home.component";
import {ManageComponent} from "./manage/manage.component";
import {AuthGuard} from "./service/security/auth.guard";
import {LoginComponent} from "./login/login.component";
import {AuswahlComponent} from "./auswahl/auswahl.component";
import {LidlOffersComponent} from "./lidl-offers/lidl-offers.component";
import {LidlOfferDetailComponent} from "./lidl-offers/lidl-offer-detail.component";


const routes: Routes = [
  {path: '', component: HomeComponent},
  {path: 'lidl', component: LidlOffersComponent},
  {path: 'lidl/:id', component: LidlOfferDetailComponent},
  {path: 'auswahl', component: AuswahlComponent},
  {
    path: 'manage', component: ManageComponent, canActivate: [AuthGuard], data: {
      role: 'ROLE_ADMIN'
    }
  },
  {path: 'login', component: LoginComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
