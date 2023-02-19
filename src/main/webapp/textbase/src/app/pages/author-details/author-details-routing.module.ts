import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { AuthorDetailsPage } from './author-details.page';

const routes: Routes = [
  {
    path: '',
    component: AuthorDetailsPage
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AuthorDetailsPageRoutingModule {}
