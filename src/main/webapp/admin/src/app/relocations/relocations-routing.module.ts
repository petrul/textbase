import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { RelocationsPage } from './relocations.page';

const routes: Routes = [
  {
    path: '',
    component: RelocationsPage
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class RelocationsPageRoutingModule {}
