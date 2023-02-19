import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { TeiRepositoriesPage } from './tei-repositories.page';

const routes: Routes = [
  {
    path: '',
    component: TeiRepositoriesPage
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class TeiRepositoriesPageRoutingModule {}
